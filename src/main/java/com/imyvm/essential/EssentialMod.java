package com.imyvm.essential;

import com.imyvm.essential.commands.CommandRegistry;
import com.imyvm.essential.data.PlayerData;
import com.imyvm.essential.systems.afk.AfkSystem;
import com.imyvm.essential.systems.fly.FlySystem;
import com.imyvm.essential.systems.ptt.PlayTimeTrackSystem;
import com.imyvm.hoki.nbt.PersistentStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class EssentialMod implements ModInitializer {
    public static final String MOD_ID = "imyvm_essential";
    public static final Logger LOGGER = LoggerFactory.getLogger("Essential");
    public static final ModConfig CONFIG = new ModConfig();
    public static final LazyTicker LAZY_TICKER = new LazyTicker(20);
    public static final PersistentStorage<PlayerData> PLAYER_DATA_STORAGE =
        new PersistentStorage<>(MOD_ID, (uuid) -> new PlayerData());

    @Override
    public void onInitialize() {
        CONFIG.loadAndSave();
        taxLoadOrReload();
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);
        this.registerSystems();
        LOGGER.info("Imyvm Essential initialized");
    }

    public void registerSystems() {
        new AfkSystem().register();
        PlayTimeTrackSystem.getInstance().register();
        FlySystem.getInstance().register();
    }
    public static void taxLoadOrReload(){
        String taxValue = CONFIG.TAX.getValue();
        String[] parts = taxValue.split(",");
        HashMap<String, Double> tax = new HashMap<>();
        for (String value : parts){
            String[] partsValue = value.split(":", 2);
            tax.put(partsValue[0], Double.valueOf(partsValue.length > 1 ? partsValue[1] : "0"));
        }
        for (TradeTypeRegistry.TradeType tradeType : TradeTypeRegistry.TradeType.values()){
            tradeType.setTax(tax.get(tradeType.name()));
        }
    }
}
