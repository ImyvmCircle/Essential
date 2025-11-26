package com.imyvm.essential.systems.fly;

import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.util.TimeUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.List;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;
import static com.imyvm.essential.commands.CommandRegistry.PAID_FLY_COMMAND;

public class PaidFlyGui implements LazyTicker.LazyTickable {
    private final ServerPlayerEntity player;
    private final SimpleGui gui;
    private FlySession session;

    public PaidFlyGui(ServerPlayerEntity player) {
        this.player = player;
        this.session = FlySystem.getInstance().getSession(player);

        this.gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false) {
            @Override
            public void onClose() {
                FlySystem.getInstance().removeGui(PaidFlyGui.this);
                super.onClose();
            }
        };

        this.setIcons();
        this.gui.setTitle(tr("gui.paid_fly.title"));
    }

    public void open() {
        this.gui.open();
        FlySystem.getInstance().addGui(this);
    }

    private void setIcons() {
        this.setHourlyIcon();  // slot 0

        this.setGoodsIconByPurchaseType(1, PurchaseType.INTRA_WORLD);
        this.setGoodsIconByPurchaseType(2, PurchaseType.INTER_WORLD);
        this.setGoodsIconByPurchaseType(3, PurchaseType.LIFETIME);

        this.setIcon(8,
            parseItem(CONFIG.FLY_CANCEL_ICON.getValue()),
            tr("gui.paid_fly.cancel.name"),
            false,
            null,
            this.wrapExecute(() -> PAID_FLY_COMMAND.executeCancel(this.player)));
    }

    private void setGoodsIconByPurchaseType(int slot, PurchaseType type) {
        boolean purchased = this.session != null && this.session.getType() == type;
        Text lore = purchased ? tr("gui.paid_fly.goods.purchased") : null;

        Runnable executor = this.wrapExecute(() -> PAID_FLY_COMMAND.universalBuy(this.player, type));
        this.setGoodsIcon(slot, type, purchased, lore, executor);
    }

    private void setHourlyIcon() {
        Text lore = null;
        boolean purchased = this.session != null && this.session.getType() == PurchaseType.HOURLY;

        if (purchased) {
            int duration = (int) (this.session.getTimeLeft() / 1000);
            lore = tr("gui.paid_fly.goods.purchased.hourly", TimeUtil.formatDuration(duration));
        }

        Runnable executor = this.wrapExecute(() -> PAID_FLY_COMMAND.executeBuyHourly(this.player, 1));
        this.setGoodsIcon(0, PurchaseType.HOURLY, purchased, lore, executor);
    }

    private void setGoodsIcon(int slot, PurchaseType type, boolean enchanted, Text lore, Runnable callback) {
        Item item = parseItem(type.getGuiIconName());
        Text name = type.getName();
        int price = type.getPrice();
        Text priceText = tr("gui.paid_fly.goods.price", MoneyUtil.format(price));

        this.setIcon(slot, item, name, enchanted, new Text[]{ priceText, lore }, callback);
    }

    private void setIcon(int slot, Item item, Text name, boolean enchanted, Text[] loreList, Runnable callback) {
        ItemStack itemStack = item.getDefaultStack();
        itemStack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, name);
        if (enchanted) {
            RegistryWrapper<Enchantment> enchantmentRegistry = this.player.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
            itemStack.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.MENDING), 1);
        }

        if (loreList != null && loreList.length > 0) {
            List<Text> lore = new ArrayList<>();
            for (Text line : loreList) {
                if (line != null) {
                    lore.add(line);
                }
            }
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }

        this.gui.setSlot(slot, itemStack, (index, type, action, gui) -> callback.run());
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        FlySession session = FlySystem.getInstance().getSession(this.player);
        if (session != this.session) {
            this.session = session;
            this.setIcons();
        } else
            this.setHourlyIcon();
    }

    private static Item parseItem(String id) {
        return Registries.ITEM.getOrEmpty(Identifier.tryParse(id)).orElseThrow();
    }

    private Runnable wrapExecute(CommandExecutor function) {
        return () -> {
            try {
                function.run();
                this.setIcons();  // update icons after action is handled
            } catch (CommandSyntaxException e) {
                this.player.sendMessage((Text) e.getRawMessage());
            }
        };
    }

    @FunctionalInterface
    private interface CommandExecutor {
        void run() throws CommandSyntaxException;
    }
}
