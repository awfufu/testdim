package com.awfufu.testdimension.data;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

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
                    return DimensionTypeConfig.fromJson(new String(readerToBytes(reader)));
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
                    JsonObject obj = GSON.fromJson(new String(readerToBytes(reader)), JsonObject.class);
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

    private static byte[] readerToBytes(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = reader.read(buf)) != -1) {
            sb.append(buf, 0, n);
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
