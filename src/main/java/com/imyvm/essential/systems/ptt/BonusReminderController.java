package com.imyvm.essential.systems.ptt;

import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static com.imyvm.essential.EssentialMod.LOGGER;
import static com.imyvm.essential.Translator.tr;

final class BonusReminderController {
    private final ServerPlayer player;
    private final TimeCounter counter;
    private final TrackData data;
    private final PendingBonus pendingBonus;
    private final ServerBossEvent bossBar;
    private boolean firstTick = true;

    BonusReminderController(ServerPlayer player, TimeCounter counter, TrackData data, PendingBonus pendingBonus,
                            ReminderPolicy policy) {
        this.player = player;
        this.counter = counter;
        this.data = data;
        this.pendingBonus = pendingBonus;
        ServerBossEvent createdBossBar = null;
        try {
            createdBossBar = new ServerBossEvent(UUID.randomUUID(), Component.empty(),
                colorFor(policy.stage(pendingBonus.getRemainingMillis(), pendingBonus.getInitialLimitMillis())),
                BossEvent.BossBarOverlay.PROGRESS);
            createdBossBar.addPlayer(player);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to create PTT bonus boss bar {} for player {}",
                counter.getTypeId(), player.getUUID(), exception);
        }
        this.bossBar = createdBossBar;
        this.updateBossBar(policy);
    }

    TickResult tick(long elapsedMillis, ReminderPolicy policy, boolean soundEnabled) {
        if (this.data.getPendingBonus() != this.pendingBonus)
            return TickResult.STALE;

        long effectiveElapsedMillis = this.firstTick ? 0L : elapsedMillis;
        this.firstTick = false;
        PendingBonus.TickWindow window = this.pendingBonus.elapse(effectiveElapsedMillis);
        this.updateBossBar(policy);
        if (window.currentMillis() <= 0L)
            return TickResult.EXPIRED;

        Optional<ReminderStage> crossedStage = policy.crossedStage(
            window.previousMillis(), window.currentMillis(), this.pendingBonus.getInitialLimitMillis());
        boolean sentStageReminder = false;
        if (crossedStage.isPresent() && crossedStage.get() != ReminderStage.EXPIRED) {
            ReminderStage stage = crossedStage.get();
            this.sendReminder("message.ptt.bonus.reminder." + stage.name().toLowerCase());
            if (soundEnabled)
                this.playStageSound(stage);
            sentStageReminder = true;
        }

        if (!sentStageReminder) {
            OptionalInt chatMilestone = policy.crossedChatMilestone(
                window.previousMillis(), window.currentMillis());
            chatMilestone.ifPresent(this::sendCountdownReminder);

            OptionalInt soundSecond = policy.crossedSoundSecond(
                window.previousMillis(), window.currentMillis(), soundEnabled);
            soundSecond.ifPresent(this::playCountdownSound);
        }

        return TickResult.ACTIVE;
    }

    void playCreatedSound() {
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.2F);
    }

    void playCriticalResumeSound() {
        this.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8F, 1.5F);
    }

    void playExpiredSound() {
        this.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F, 0.7F);
    }

    boolean isCritical(ReminderPolicy policy) {
        return policy.stage(this.pendingBonus.getRemainingMillis(),
            this.pendingBonus.getInitialLimitMillis()) == ReminderStage.CRITICAL;
    }

    PendingBonus getPendingBonus() {
        return this.pendingBonus;
    }

    ServerPlayer getPlayer() {
        return this.player;
    }

    TimeCounter getCounter() {
        return this.counter;
    }

    TrackData getData() {
        return this.data;
    }

    void close() {
        if (this.bossBar == null)
            return;
        try {
            this.bossBar.removeAllPlayers();
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to remove PTT bonus boss bar {} for player {}",
                this.counter.getTypeId(), this.player.getUUID(), exception);
        }
    }

    private void updateBossBar(ReminderPolicy policy) {
        if (this.bossBar == null)
            return;

        try {
            long seconds = Math.max(0L, (this.pendingBonus.getRemainingMillis() + 999L) / 1000L);
            Component name = tr("name.bonus." + this.counter.getTypeId());
            this.bossBar.setName(tr("message.ptt.bonus.bossbar", name,
                MoneyUtil.format(this.pendingBonus.getBonus()),
                TimeUtil.formatDuration((int) Math.min(Integer.MAX_VALUE, seconds))));
            float progress = (float) this.pendingBonus.getRemainingMillis()
                / this.pendingBonus.getInitialLimitMillis();
            this.bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, progress)));
            this.bossBar.setColor(colorFor(policy.stage(this.pendingBonus.getRemainingMillis(),
                this.pendingBonus.getInitialLimitMillis())));
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to update PTT bonus boss bar {} for player {}",
                this.counter.getTypeId(), this.player.getUUID(), exception);
        }
    }

    private void sendReminder(String translationKey) {
        this.player.sendSystemMessage(tr(translationKey,
            tr("name.bonus." + this.counter.getTypeId()),
            BonusSupplier.createAcquireCommand(this.counter, this.pendingBonus)));
    }

    private void sendCountdownReminder(int seconds) {
        this.player.sendSystemMessage(tr("message.ptt.bonus.reminder.countdown",
            tr("name.bonus." + this.counter.getTypeId()), seconds,
            BonusSupplier.createAcquireCommand(this.counter, this.pendingBonus)));
    }

    private void playStageSound(ReminderStage stage) {
        float pitch = stage == ReminderStage.CRITICAL ? 1.5F : 1.1F;
        this.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8F, pitch);
    }

    private void playCountdownSound(int seconds) {
        if (seconds <= 1) {
            this.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.9F, 1.8F);
            return;
        }

        float pitch = Math.min(1.8F, 0.9F + (6 - Math.min(seconds, 5)) * 0.18F);
        this.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 0.7F, pitch);
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        try {
            this.player.playSound(sound, volume, pitch);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to play PTT bonus sound {} for player {}",
                this.counter.getTypeId(), this.player.getUUID(), exception);
        }
    }

    private static BossEvent.BossBarColor colorFor(ReminderStage stage) {
        return switch (stage) {
            case NORMAL -> BossEvent.BossBarColor.GREEN;
            case WARNING -> BossEvent.BossBarColor.YELLOW;
            case CRITICAL, EXPIRED -> BossEvent.BossBarColor.RED;
        };
    }

    enum TickResult {
        ACTIVE,
        EXPIRED,
        STALE,
    }
}
