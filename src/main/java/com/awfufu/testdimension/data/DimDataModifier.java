package com.awfufu.testdimension.data;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class DimDataModifier {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static DimensionTypeConfig currentTypeConfig = new DimensionTypeConfig();
    private static DimensionGeneratorConfig currentDimConfig = new DimensionGeneratorConfig();

    private static String overridingPackName = null;

    private DimDataModifier() {
    }


    public static String buildTypeJson() {
        return currentTypeConfig.toJson();
    }

    public static String buildDimensionJson() {
        JsonObject root = new JsonObject();
        root.addProperty("type", TestDimensionKeys.TEST_WORLD_TYPE_ID().toString());
        root.add("generator", currentDimConfig.toJsonObject());
        return GSON.toJson(root);
    }

    public static void loadDefaultConfigsFromClasspath() {
        try (InputStream typeStream = DimDataModifier.class.getClassLoader()
                .getResourceAsStream("/data/testdim/dimension_type/test_type.json")) {
            if (typeStream != null) {
                try (Reader reader = new InputStreamReader(typeStream, StandardCharsets.UTF_8)) {
                    currentTypeConfig = DimensionTypeConfig.fromJson(readerToString(reader));
                    TestDimensionMod.LOGGER.info("Loaded built-in dimension type config from classpath");
                }
            } else {
                TestDimensionMod.LOGGER.warn("Built-in dimension type config not found on classpath");
            }
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to load built-in dimension type config from classpath", e);
        }

        try (InputStream dimStream = DimDataModifier.class.getClassLoader()
                .getResourceAsStream("/data/testdim/dimension/test.json")) {
            if (dimStream != null) {
                try (Reader reader = new InputStreamReader(dimStream, StandardCharsets.UTF_8)) {
                    JsonObject obj = GSON.fromJson(readerToString(reader), JsonObject.class);
                    if (obj.has("generator")) {
                        currentDimConfig = new Gson().fromJson(obj.get("generator"), DimensionGeneratorConfig.class);
                    }
                    TestDimensionMod.LOGGER.info("Loaded built-in dimension config from classpath");
                }
            } else {
                TestDimensionMod.LOGGER.warn("Built-in dimension config not found on classpath");
            }
        } catch (IOException e) {
            TestDimensionMod.LOGGER.error("Failed to load built-in dimension config from classpath", e);
        }
    }

    public static void detectAndApplyDatapackOverrides(MinecraftServer server) {
        String typePath = "dimension_type/" + TestDimensionKeys.TEST_WORLD_TYPE_ID().getPath() + ".json";
        ResourceLocation typeLoc = ResourceLocation.fromNamespaceAndPath(
                TestDimensionKeys.TEST_WORLD_TYPE_ID().getNamespace(), typePath);

        String dimPath = "dimension/" + TestDimensionKeys.TEST_WORLD_ID().getPath() + ".json";
        ResourceLocation dimLoc = ResourceLocation.fromNamespaceAndPath(
                TestDimensionKeys.TEST_WORLD_ID().getNamespace(), dimPath);

        boolean overridden = false;

        try {
            Optional<Resource> typeRes = server.getResourceManager().getResource(typeLoc);
            if (typeRes.isPresent()) {
                String sourcePack = typeRes.get().source().packId();
                if (!sourcePack.contains("testdimension")) {
                    TestDimensionMod.LOGGER.info("Datapack override detected for dimension type (source: {})", sourcePack);
                    try (Reader reader = typeRes.get().openAsReader()) {
                        currentTypeConfig = DimensionTypeConfig.fromJson(readerToString(reader));
                    }
                    overridingPackName = sourcePack;
                    overridden = true;
                }
            }
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to check datapack override for dimension type", e);
        }

        try {
            Optional<Resource> dimRes = server.getResourceManager().getResource(dimLoc);
            if (dimRes.isPresent()) {
                String sourcePack = dimRes.get().source().packId();
                if (!sourcePack.contains("testdimension")) {
                    TestDimensionMod.LOGGER.info("Datapack override detected for dimension (source: {})", sourcePack);
                    try (Reader reader = dimRes.get().openAsReader()) {
                        JsonObject obj = GSON.fromJson(readerToString(reader), JsonObject.class);
                        if (obj.has("generator")) {
                            currentDimConfig = new Gson().fromJson(obj.get("generator"), DimensionGeneratorConfig.class);
                        }
                    }
                    if (overridingPackName == null) {
                        overridingPackName = sourcePack;
                    }
                    overridden = true;
                }
            }
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to check datapack override for dimension", e);
        }

        if (overridden) {
            TestDimensionMod.LOGGER.info("Applied datapack overrides from pack: {}", overridingPackName);
        } else {
            TestDimensionMod.LOGGER.info("No datapack override detected, using built-in configs");
        }
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

        return Path.of(DimDataModifier.class.getClassLoader().getResource("/data/testdim/dimension_type/test_type.json").getPath());
    }

    public static Path getDefaultDimensionConfigPath() {
        return Path.of(DimDataModifier.class.getClassLoader().getResource("/data/testdim/dimension/test.json").getPath());
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
}
