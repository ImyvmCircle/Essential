package com.imyvm.essential.control;

import com.imyvm.essential.EssentialMod;
import com.imyvm.essential.ImmediatelyTranslator;
import com.imyvm.essential.interfaces.LazyTickable;
import com.imyvm.essential.interfaces.PlayerEntityMixinInterface;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class TeleportRequest implements LazyTickable {
    private final ServerPlayerEntity sender;
    private final ServerPlayerEntity receiver;
    private final ServerPlayerEntity beingTeleported;
    private final TeleportType type;
    private Vec3d source;
    private Vec3d destination;
    private ServerWorld destinationWorld;
    private long issueAt;
    private long acceptAt;

    public TeleportRequest(ServerPlayerEntity sender, ServerPlayerEntity receiver, TeleportType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;

        this.beingTeleported = switch (this.type) {
            case TELEPORT_TO -> this.sender;
            case TELEPORT_HERE -> this.receiver;
        };
    }

    public boolean issue() {
        if (((PlayerEntityMixinInterface) this.sender).imyvm$getRequestAsSender() != null) {
            this.sender.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.failed.has_pending.self"));
            return false;
        }
        if (((PlayerEntityMixinInterface) this.receiver).imyvm$getRequestAsReceiver() != null) {
            this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.failed.has_pending.others", this.receiver.getName()));
            return false;
        }

        ((PlayerEntityMixinInterface) this.sender).imyvm$setRequestAsSender(this);
        ((PlayerEntityMixinInterface) this.receiver).imyvm$setRequestAsReceiver(this);

        this.sender.sendMessage(ImmediatelyTranslator.translatable(this.type.issueMessageKey + ".sender", this.receiver.getName()));

        MutableText buttons = Text.empty().copy();
        MutableText accept = ((MutableText) ImmediatelyTranslator.translatable("commands.tpa.button.accept"))
            .styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept")));
        MutableText reject = ((MutableText) ImmediatelyTranslator.translatable("commands.tpa.button.reject"))
            .styled(style -> style.withColor(Formatting.RED).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpreject")));
        buttons.append(accept).append(Text.of("  ")).append(reject);

        this.receiver.sendMessage(ImmediatelyTranslator.translatable(this.type.issueMessageKey + ".receiver", this.sender.getName()));
        this.receiver.sendMessage(buttons);

        this.issueAt = System.currentTimeMillis();
        EssentialMod.teleportRequests.add(this);

        return true;
    }

    public void cancel() {
        this.sender.sendMessage(ImmediatelyTranslator.translatable("commands.tpcancel.sender"));
        this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpcancel.receiver", this.sender.getName()));
        this.finish();
    }

    public void accept() {
        if (this.isBeingTeleported()) {
            this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpaccept.failed.already_accepted"));
            return;
        }

        this.source = this.beingTeleported.getPos();
        this.acceptAt = System.currentTimeMillis();

        ServerPlayerEntity destinationPlayer = this.sender == this.beingTeleported ? this.receiver : this.sender;
        this.destination = destinationPlayer.getPos();
        this.destinationWorld = destinationPlayer.getWorld();

        this.sender.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.accept.sender", this.receiver.getName()));
        this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.accept.receiver", this.sender.getName()));
        this.beingTeleported.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.message.being_teleport",
            (EssentialMod.config.getTeleportWait() + 500) / 1000));
    }

    public void performTeleportation() {
        this.beingTeleported.teleport(destinationWorld, destination.x, destination.y, destination.z, this.beingTeleported.getYaw(), this.beingTeleported.getPitch());
        this.finish();
    }

    public void reject() {
        this.sender.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.reject.sender", this.receiver.getName()));
        this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.reject.receiver", this.sender.getName()));
        this.finish();
    }

    @Override
    public void imyvm$lazyTick() {
        if (this.isBeingTeleported() && this.isBeingTeleportedPlayerMoved()) {
            this.beingTeleported.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.failed.move_when_wait"));
            this.finish();
            return;
        }

        if (this.shouldPerformTeleportation())
            this.performTeleportation();

        if (!this.isBeingTeleported() && this.isTimeout()) {
            this.sender.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.failed.sender.timeout"));
            this.receiver.sendMessage(ImmediatelyTranslator.translatable("commands.tpa.failed.receiver.timeout"));
            this.finish();
        }
    }

    private void finish() {
        ((PlayerEntityMixinInterface) this.sender).imyvm$setRequestAsSender(null);
        ((PlayerEntityMixinInterface) this.receiver).imyvm$setRequestAsReceiver(null);
        EssentialMod.teleportRequests.remove(this);
    }

    private boolean isTimeout() {
        return System.currentTimeMillis() - this.issueAt > EssentialMod.config.getTeleportTimeout();
    }

    private boolean isBeingTeleported() {
        return this.acceptAt != 0;
    }

    private boolean isBeingTeleportedPlayerMoved() {
        return this.source.squaredDistanceTo(this.beingTeleported.getPos()) > 0.25;
    }

    private boolean shouldPerformTeleportation() {
        return this.isBeingTeleported() && System.currentTimeMillis() - this.acceptAt > EssentialMod.config.getTeleportWait();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleportRequest that = (TeleportRequest) o;
        return this.sender.getName().equals(that.sender.getName()) && this.receiver.getName().equals(that.receiver.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sender.getName(), this.receiver.getName());
    }

    public enum TeleportType {
        TELEPORT_TO("commands.tpa.issue"),
        TELEPORT_HERE("commands.tpahere.issue");

        public final String issueMessageKey;

        TeleportType(String issueMessageKey) {
            this.issueMessageKey = issueMessageKey;
        }
    }
}
