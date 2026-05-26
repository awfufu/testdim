package com.awfufu.testdimension.data;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DimDataModifier {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static DimensionTypeConfig currentTypeConfig = new DimensionTypeConfig();
    private static DimensionGeneratorConfig currentDimConfig = new DimensionGeneratorConfig();

    private DimDataModifier() {
    }

    public static DimensionTypeConfig getCurrentTypeConfig() {
        return new DimensionTypeConfig(currentTypeConfig);
    }

    public static DimensionGeneratorConfig getCurrentDimConfig() {
        return new DimensionGeneratorConfig(currentDimConfig);
    }

    public static void updateTypeConfig(DimensionTypeConfig config) {
        currentTypeConfig = new DimensionTypeConfig(config);
    }

    public static void updateDimConfig(DimensionGeneratorConfig config) {
        currentDimConfig = new DimensionGeneratorConfig(config);
    }

    public static String buildTypeJson() {
        return currentTypeConfig.toJson();
    }

    public static String buildDimensionJson() {
        JsonObject root = new JsonObject();
        root.addProperty("type", TestDimensionKeys.TEST_WORLD_TYPE_ID.toString());
        root.add("generator", currentDimConfig.toJsonObject());
        return GSON.toJson(root);
    }

    public static DimensionTypeConfig loadTypeConfigFromDatapack(MinecraftServer server, ResourceLocation dimensionTypeId) {
        String path = "dimension_type/" + dimensionTypeId.getPath() + ".json";
        ResourceLocation resourceLoc = ResourceLocation.fromNamespaceAndPath(dimensionTypeId.getNamespace(), path);
        try {
            Optional<Resource> resource = server.getResourceManager().getResource(resourceLoc);
            if (resource.isPresent()) {
                try (Reader reader = resource.get().openAsReader()) {
                    return DimensionTypeConfig.fromJson(readerToString(reader));
                }
            }
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to load dimension type config from datapack: {}", resourceLoc, e);
        }
        return null;
    }

    public static DimensionGeneratorConfig loadDimConfigFromDatapack(MinecraftServer server, ResourceLocation dimensionId) {
        String path = "dimension/" + dimensionId.getPath() + ".json";
        ResourceLocation resourceLoc = ResourceLocation.fromNamespaceAndPath(dimensionId.getNamespace(), path);
        try {
            Optional<Resource> resource = server.getResourceManager().getResource(resourceLoc);
            if (resource.isPresent()) {
                try (Reader reader = resource.get().openAsReader()) {
                    JsonObject obj = GSON.fromJson(readerToString(reader), JsonObject.class);
                    DimensionGeneratorConfig config = new DimensionGeneratorConfig();
                    if (obj.has("generator")) {
                        config = new Gson().fromJson(obj.get("generator"), DimensionGeneratorConfig.class);
                    }
                    return config;
                }
            }
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to load dimension config from datapack: {}", resourceLoc, e);
        }
        return null;
    }

    public static boolean saveTypeConfigToFile(Path outputPath) {
        try {
            String json = buildTypeJson();
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, json);
            TestDimensionMod.LOGGER.info("Saved dimension type config to {}", outputPath);
            return true;
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to save dimension type config", e);
            return false;
        }
    }

    public static boolean saveDimensionConfigToFile(Path outputPath) {
        try {
            String json = buildDimensionJson();
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, json);
            TestDimensionMod.LOGGER.info("Saved dimension config to {}", outputPath);
            return true;
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to save dimension config", e);
            return false;
        }
    }

    public static boolean loadTypeConfigFromFile(Path inputPath) {
        try {
            String json = Files.readString(inputPath);
            currentTypeConfig = DimensionTypeConfig.fromJson(json);
            TestDimensionMod.LOGGER.info("Loaded dimension type config from {}", inputPath);
            return true;
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to load dimension type config from {}", inputPath, e);
            return false;
        }
    }

    public static boolean loadDimensionConfigFromFile(Path inputPath) {
        try {
            String json = Files.readString(inputPath);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj.has("generator")) {
                currentDimConfig = new Gson().fromJson(obj.get("generator"), DimensionGeneratorConfig.class);
            }
            TestDimensionMod.LOGGER.info("Loaded dimension config from {}", inputPath);
            return true;
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to load dimension config from {}", inputPath, e);
            return false;
        }
    }

    public static Path getDefaultTypeConfigPath() {
        return Paths.get("src", "main", "resources", "data", "testdim", "dimension_type", "test_type.json");
    }

    public static Path getDefaultDimensionConfigPath() {
        return Paths.get("src", "main", "resources", "data", "testdim", "dimension", "test.json");
    }

    public static String reloadTestDimension(MinecraftServer server) {
        saveTypeConfigToFile(getDefaultTypeConfigPath());
        saveDimensionConfigToFile(getDefaultDimensionConfigPath());

        try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
            TestDimensionMod.LOGGER.info("Executed /reload after saving dimension config");
        } catch (Exception e) {
            TestDimensionMod.LOGGER.warn("Failed to execute /reload: {}", e.getMessage());
        }

        ServerLevel level = server.getLevel(TestDimensionKeys.TEST_WORLD);
        if (level == null) {
            return "Config saved. Test dimension not loaded yet - new settings on first entry.";
        }

        int evacuated = evacuatePlayers(level, server);
        String result = applyFlatGeneratorSettings(level, server);

        return result + (evacuated > 0 ? " (" + evacuated + " players evacuated)" : "");
    }

    private static int evacuatePlayers(ServerLevel level, MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;
        int count = 0;
        for (ServerPlayer player : List.copyOf(level.players())) {
            player.teleportTo(overworld, player.getX(), overworld.getMaxBuildHeight(),
                    player.getZ(), java.util.Set.of(), player.getYRot(), player.getXRot());
            count++;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static String applyFlatGeneratorSettings(ServerLevel level, MinecraftServer server) {
        try {
            ChunkGenerator generator = level.getChunkSource().getGenerator();
            if (!(generator instanceof FlatLevelSource flatSource)) {
                return "Config saved. Generator is not flat - restart required.";
            }

            FlatLevelGeneratorSettings settings = getFlatSettings(flatSource);
            if (settings == null) {
                return "Config saved. Could not access flat generator settings.";
            }

            updateFlatLayers(settings, server);
            updateFlatBiome(settings, server);
            updateFlatDecoration(settings);

            TestDimensionMod.LOGGER.info("Hot-reloaded flat generator settings in-place");
            return "Generator hot-reloaded - new chunks will use updated settings.";
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to hot-reload generator settings", e);
            return "Config saved but generator hot-reload failed: " + e.getMessage();
        }
    }

    private static FlatLevelGeneratorSettings getFlatSettings(FlatLevelSource flatSource) {
        try {
            Field settingsField = findField(FlatLevelSource.class, "settings");
            settingsField.setAccessible(true);
            Holder<FlatLevelGeneratorSettings> holder =
                    (Holder<FlatLevelGeneratorSettings>) settingsField.get(flatSource);
            return holder.value();
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to read flat source settings field: {}", e.getMessage());
            return null;
        }
    }

    private static void updateFlatLayers(FlatLevelGeneratorSettings settings, MinecraftServer server) {
        try {
            Field layersField = findField(FlatLevelGeneratorSettings.class, "layers");
            layersField.setAccessible(true);
            List<FlatLayerInfo> layers = (List<FlatLayerInfo>) layersField.get(settings);
            layers.clear();

            for (var layerCfg : currentDimConfig.flatSettings.layers) {
                ResourceLocation blockId = ResourceLocation.tryParse(layerCfg.block);
                if (blockId == null) continue;
                Block block = server.registryAccess().registryOrThrow(Registries.BLOCK).get(blockId);
                if (block != null) {
                    layers.add(new FlatLayerInfo(layerCfg.height, block));
                }
            }
            TestDimensionMod.LOGGER.info("Updated flat layers: {} entries", layers.size());
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to update flat layers: {}", e.getMessage());
        }
    }

    private static void updateFlatBiome(FlatLevelGeneratorSettings settings, MinecraftServer server) {
        try {
            ResourceLocation biomeId = ResourceLocation.tryParse(currentDimConfig.flatSettings.biome);
            if (biomeId == null) return;

            Registry<Biome> biomeReg = server.registryAccess().registryOrThrow(Registries.BIOME);
            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, biomeId);
            Optional<Holder.Reference<Biome>> holderOpt = biomeReg.getHolder(key);
            if (holderOpt.isEmpty()) return;

            Field biomeField = findField(FlatLevelGeneratorSettings.class, "biome");
            biomeField.setAccessible(true);
            biomeField.set(settings, holderOpt.get());
            TestDimensionMod.LOGGER.info("Updated flat biome to {}", biomeId);
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to update flat biome: {}", e.getMessage());
        }
    }

    private static void updateFlatDecoration(FlatLevelGeneratorSettings settings) {
        try {
            Field decorationField = findField(FlatLevelGeneratorSettings.class, "decoration");
            decorationField.setAccessible(true);
            Object decoObj = decorationField.get(settings);
            if (decoObj == null) return;

            for (Field f : decoObj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getName().equals("lakes")) {
                    f.setBoolean(decoObj, currentDimConfig.flatSettings.lakes);
                } else if (f.getName().equals("features")) {
                    f.setBoolean(decoObj, currentDimConfig.flatSettings.features);
                }
            }
            TestDimensionMod.LOGGER.info("Updated flat decoration: lakes={}, features={}",
                    currentDimConfig.flatSettings.lakes, currentDimConfig.flatSettings.features);
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to update flat decoration: {}", e.getMessage());
        }
    }

    private static String readerToString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = reader.read(buf)) != -1) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " not found in " + clazz);
    }
}
