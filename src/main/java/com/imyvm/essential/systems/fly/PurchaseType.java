package com.imyvm.essential.systems.fly;

import net.minecraft.text.Text;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;
import static com.imyvm.essential.Translator.tr;

public enum PurchaseType {
    HOURLY("hourly", CONFIG.FLY_HOURLY_PRICE::getValue),
    INTRA_WORLD("intra_world", CONFIG.FLY_INTRA_WORLD_PRICE::getValue),
    INTER_WORLD("inter_world", CONFIG.FLY_INTER_WORLD_PRICE::getValue),
    LIFETIME("lifetime", CONFIG.FLY_LIFETIME_PRICE::getValue);

    private final String id;
    private final Supplier<Integer> priceSupplier;

    PurchaseType(String id, Supplier<Integer> priceSupplier) {
        this.id = id;
        this.priceSupplier = priceSupplier;
    }

    public Text getName() {
        return tr("goods.paid_fly." + this.getId(), 1);
    }

    public int getPrice() {
        return this.priceSupplier.get();
    }

    public String getId() {
        return this.id;
    }
}
