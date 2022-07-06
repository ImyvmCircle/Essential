package com.imyvm.essential.data;

import com.imyvm.essential.EssentialMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.imyvm.essential.EssentialMod.LOGGER;

public class PlayerDataStorage {
    private static final Path basePath = getBasePath();
    private static final Map<UUID, PlayerData> loadedPlayerData = new ConcurrentHashMap<>();

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> saveAll());
    }

    public static void saveAll() {
        loadedPlayerData.forEach((uuid, playerData) -> {
            File file = getDataFile(uuid);
            NbtCompound nbt = new NbtCompound();
            playerData.writeNbt(nbt);

            try {
                NbtIo.writeCompressed(nbt, file);
            } catch (IOException e) {
                LOGGER.error("Error while saving player data", e);
            }
        });
    }

    public static PlayerData getOrCreate(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerData playerData;
        if ((playerData = loadedPlayerData.get(uuid)) != null)
            return playerData;

        playerData = loadDataFromDisk(uuid);
        if (playerData == null)
            playerData = new PlayerData();
        loadedPlayerData.put(uuid, playerData);

        return playerData;
    }

    private static PlayerData loadDataFromDisk(UUID uuid) {
        File file = getDataFile(uuid);
        if (!file.exists())
            return null;

        PlayerData playerData = new PlayerData();
        try {
            NbtCompound nbt = NbtIo.readCompressed(file);
            playerData.loadNbt(nbt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return playerData;
    }

    private static File getDataFile(UUID uuid) {
        return basePath.resolve(uuid.toString() + ".dat").toFile();
    }

    private static Path getBasePath() {
        try {
            Path basePath = FabricLoader.getInstance().getGameDir().resolve("world").resolve(EssentialMod.MOD_ID);
            return Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
