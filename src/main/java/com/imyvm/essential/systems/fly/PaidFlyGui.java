package com.imyvm.essential.systems.fly;

import com.imyvm.economy.util.MoneyUtil;
import com.imyvm.essential.LazyTicker;
import com.imyvm.essential.util.TimeUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.imyvm.essential.commands.CommandRegistry.PAID_FLY_COMMAND;
import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

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

        this.setIcon(8, parseItem(CONFIG.FLY_CANCEL_ICON.getValue()), tr("gui.paid_fly.cancel.name"), false, null, this.wrapExecute(() -> PAID_FLY_COMMAND.executeCancel(this.player)));
    }

    private void setGoodsIconByPurchaseType(int slot, PurchaseType type) {
        boolean purchased = this.session != null && this.session.getType() == type;
        Text lore = purchased ? tr("gui.paid_fly.goods.purchased") : null;
        Item item = Registry.ITEM.get(Identifier.tryParse(type.getGuiIconName()));

        this.setGoodsIcon(slot, item, type.getName(), purchased, type.getPrice(), lore, this.wrapExecute(() -> PAID_FLY_COMMAND.universalBuy(this.player, type)));
    }

    private void setHourlyIcon() {
        Text lore = null;
        boolean purchased = this.session != null && this.session.getType() == PurchaseType.HOURLY;

        if (purchased)
            lore = tr("gui.paid_fly.goods.purchased.hourly", TimeUtil.formatDuration((int) (this.session.getTimeLeft() / 1000)));
        Item item = parseItem(PurchaseType.HOURLY.getGuiIconName());

        this.setGoodsIcon(0, item, PurchaseType.HOURLY.getName(), purchased, PurchaseType.HOURLY.getPrice(), lore, this.wrapExecute(() -> PAID_FLY_COMMAND.executeBuyHourly(this.player, 1)));
    }

    private void setGoodsIcon(int slot, Item item, Text name, boolean enchanted, int price, Text lore, Runnable callback) {
        Text priceText = tr("gui.paid_fly.goods.price", MoneyUtil.format(price));
        this.setIcon(slot, item, name, enchanted, new Text[]{ priceText, lore }, callback);
    }

    private void setIcon(int slot, Item item, Text name, boolean enchanted, Text[] loreList, Runnable callback) {
        ItemStack itemStack = item.getDefaultStack();
        itemStack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        itemStack.setCustomName(name);
        if (enchanted)
            itemStack.addEnchantment(Enchantments.MENDING, 1);

        if (loreList != null && loreList.length > 0) {
            NbtList lore = new NbtList();
            for (Text line : loreList)
                if (line != null)
                    lore.add(NbtString.of(Text.Serializer.toJson(line)));
            itemStack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY).put(ItemStack.LORE_KEY, lore);
        }

        this.gui.setSlot(slot, itemStack, (index, type, action, gui) -> callback.run());
    }

    @Override
    public void lazyTick(MinecraftServer server, long tickCounts, long msSinceLastTick) {
        FlySession session = FlySystem.getInstance().getSession(this.player);
        if (session != this.session) {
            this.session = session;
            this.setIcons();
        }
        else
            this.setHourlyIcon();
    }

    private static Item parseItem(String id) {
        return Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).orElseThrow();
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
