package com.imyvm.essential.systems.fly;

import java.util.function.Supplier;

import static com.imyvm.essential.EssentialMod.CONFIG;

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

    public int getPrice() {
        return priceSupplier.get();
    }

    public String getId() {
        return id;
    }
}
