package com.imyvm.essential.systems.fly;

import net.minecraft.text.Text;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public enum PurchaseType {
    HOURLY("hourly", CONFIG.FLY_HOURLY_PRICE::getValue, CONFIG.FLY_HOURLY_ICON::getValue),
    INTRA_WORLD("intra_world", CONFIG.FLY_INTRA_WORLD_PRICE::getValue, CONFIG.FLY_INTRA_WORLD_ICON::getValue),
    INTER_WORLD("inter_world", CONFIG.FLY_INTER_WORLD_PRICE::getValue, CONFIG.FLY_INTER_WORLD_ICON::getValue),
    LIFETIME("lifetime", CONFIG.FLY_LIFETIME_PRICE::getValue, CONFIG.FLY_LIFETIME_ICON::getValue);

    private final String id;
    private final Supplier<Integer> priceSupplier;
    private final Supplier<String> guiIconNameSupplier;

    PurchaseType(String id, Supplier<Integer> priceSupplier, Supplier<String> guiIconNameSupplier) {
        this.id = id;
        this.priceSupplier = priceSupplier;
        this.guiIconNameSupplier = guiIconNameSupplier;
    }

    public Text getName() {
        return tr("goods.paid_fly." + this.id, 1);
    }

    public int getPrice() {
        return this.priceSupplier.get();
    }

    public String getGuiIconName() {
        return this.guiIconNameSupplier.get();
    }

    public String getId() {
        return this.id;
    }
}
