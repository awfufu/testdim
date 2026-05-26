package com.awfufu.testdimension.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class DimensionGeneratorConfig {
    public String type;
    @SerializedName("settings")
    public FlatSettings flatSettings;
    public transient String rawJson;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public DimensionGeneratorConfig() {
        this.type = "minecraft:flat";
        this.flatSettings = new FlatSettings();
    }

    public DimensionGeneratorConfig(DimensionGeneratorConfig other) {
        this.type = other.type;
        this.flatSettings = other.flatSettings != null ? new FlatSettings(other.flatSettings) : new FlatSettings();
        this.rawJson = other.rawJson;
    }

    public static DimensionGeneratorConfig fromJson(String json) {
        return new Gson().fromJson(json, DimensionGeneratorConfig.class);
    }

    public String toPrettyJson() {
        return GSON.toJson(toJsonObject());
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        if ("minecraft:flat".equals(type) && flatSettings != null) {
            obj.add("settings", flatSettings.toJsonObject());
        }
        return obj;
    }

    public static class FlatSettings {
        public List<Layer> layers;
        public String biome;
        public boolean lakes;
        public boolean features;
        @SerializedName("structure_overrides")
        public String structureOverrides;

        public FlatSettings() {
            this.layers = new ArrayList<>();
            this.layers.add(new Layer(64, "minecraft:iron_block"));
            this.biome = "minecraft:the_void";
            this.lakes = false;
            this.features = false;
            this.structureOverrides = "[]";
        }

        public FlatSettings(FlatSettings other) {
            this.layers = new ArrayList<>();
            if (other.layers != null) {
                for (Layer l : other.layers) {
                    this.layers.add(new Layer(l.height, l.block));
                }
            }
            this.biome = other.biome;
            this.lakes = other.lakes;
            this.features = other.features;
            this.structureOverrides = other.structureOverrides;
        }

        public JsonObject toJsonObject() {
            JsonObject obj = new JsonObject();
            try {
                obj.add("structure_overrides", new Gson().fromJson(structureOverrides != null ? structureOverrides : "[]", JsonArray.class));
            } catch (Exception e) {
                obj.add("structure_overrides", new JsonArray());
            }
            JsonArray layersArr = new JsonArray();
            if (layers != null) {
                for (Layer layer : layers) {
                    layersArr.add(layer.toJsonObject());
                }
            }
            obj.add("layers", layersArr);
            obj.addProperty("lakes", lakes);
            obj.addProperty("features", features);
            obj.addProperty("biome", biome);
            return obj;
        }
    }

    public static class Layer {
        public int height;
        public String block;

        public Layer() {}

        public Layer(int height, String block) {
            this.height = height;
            this.block = block;
        }

        public JsonObject toJsonObject() {
            JsonObject obj = new JsonObject();
            obj.addProperty("height", height);
            obj.addProperty("block", block);
            return obj;
        }
    }
}
