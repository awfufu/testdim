package com.awfufu.testdimension.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class DimensionTypeConfig {
    public boolean ultrawarm;
    public boolean natural;
    public double coordinate_scale;
    public boolean bed_works;
    public boolean respawn_anchor_works;
    public int min_y;
    public int height;
    public int logical_height;
    public String infiniburn;
    public String effects;
    public double ambient_light;
    public boolean piglin_safe;
    public boolean has_raids;
    public boolean has_skylight;
    public boolean has_ceiling;
    public int monster_spawn_block_light_limit;
    public String monster_spawn_light_level;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public DimensionTypeConfig() {
        this.ultrawarm = false;
        this.natural = true;
        this.coordinate_scale = 1.0;
        this.bed_works = true;
        this.respawn_anchor_works = false;
        this.min_y = -64;
        this.height = 384;
        this.logical_height = 384;
        this.infiniburn = "#minecraft:infiniburn_overworld";
        this.effects = "minecraft:overworld";
        this.ambient_light = 0.0;
        this.piglin_safe = false;
        this.has_raids = true;
        this.has_skylight = true;
        this.has_ceiling = false;
        this.monster_spawn_block_light_limit = 0;
        this.monster_spawn_light_level = "{\"type\":\"minecraft:uniform\",\"min_inclusive\":0,\"max_inclusive\":7}";
    }

    public DimensionTypeConfig(DimensionTypeConfig other) {
        this.ultrawarm = other.ultrawarm;
        this.natural = other.natural;
        this.coordinate_scale = other.coordinate_scale;
        this.bed_works = other.bed_works;
        this.respawn_anchor_works = other.respawn_anchor_works;
        this.min_y = other.min_y;
        this.height = other.height;
        this.logical_height = other.logical_height;
        this.infiniburn = other.infiniburn;
        this.effects = other.effects;
        this.ambient_light = other.ambient_light;
        this.piglin_safe = other.piglin_safe;
        this.has_raids = other.has_raids;
        this.has_skylight = other.has_skylight;
        this.has_ceiling = other.has_ceiling;
        this.monster_spawn_block_light_limit = other.monster_spawn_block_light_limit;
        this.monster_spawn_light_level = other.monster_spawn_light_level;
    }

    public static DimensionTypeConfig fromJson(String json) {
        Gson gson = new Gson();
        DimensionTypeConfig config = gson.fromJson(json, DimensionTypeConfig.class);
        if (config.monster_spawn_light_level == null) {
            config.monster_spawn_light_level = "0-7";
        }
        return config;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public String toPrettyJson() {
        JsonObject obj = newJsonObject();
        return GSON.toJson(obj);
    }

    private JsonObject newJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("ultrawarm", ultrawarm);
        obj.addProperty("natural", natural);
        obj.addProperty("coordinate_scale", coordinate_scale);
        obj.addProperty("bed_works", bed_works);
        obj.addProperty("respawn_anchor_works", respawn_anchor_works);
        obj.addProperty("min_y", min_y);
        obj.addProperty("height", height);
        obj.addProperty("logical_height", logical_height);
        obj.addProperty("infiniburn", infiniburn);
        obj.addProperty("effects", effects);
        obj.addProperty("ambient_light", ambient_light);
        obj.addProperty("piglin_safe", piglin_safe);
        obj.addProperty("has_raids", has_raids);
        obj.addProperty("has_skylight", has_skylight);
        obj.addProperty("has_ceiling", has_ceiling);
        obj.addProperty("monster_spawn_block_light_limit", monster_spawn_block_light_limit);
        if (monster_spawn_light_level != null) {
            try {
                obj.add("monster_spawn_light_level", new Gson().fromJson(monster_spawn_light_level, JsonObject.class));
            } catch (Exception e) {
                obj.addProperty("monster_spawn_light_level", monster_spawn_light_level);
            }
        }
        return obj;
    }

    public static DimensionTypeConfig createNether() {
        DimensionTypeConfig c = new DimensionTypeConfig();
        c.ultrawarm = true;
        c.natural = false;
        c.coordinate_scale = 8.0;
        c.bed_works = false;
        c.respawn_anchor_works = true;
        c.min_y = 0;
        c.height = 256;
        c.logical_height = 128;
        c.infiniburn = "#minecraft:infiniburn_nether";
        c.effects = "minecraft:the_nether";
        c.ambient_light = 0.1;
        c.piglin_safe = true;
        c.has_raids = false;
        c.has_skylight = false;
        c.has_ceiling = true;
        c.monster_spawn_block_light_limit = 15;
        c.monster_spawn_light_level = "7";
        return c;
    }

    public static DimensionTypeConfig createEnd() {
        DimensionTypeConfig c = new DimensionTypeConfig();
        c.ultrawarm = false;
        c.natural = false;
        c.coordinate_scale = 1.0;
        c.bed_works = false;
        c.respawn_anchor_works = false;
        c.min_y = 0;
        c.height = 256;
        c.logical_height = 256;
        c.infiniburn = "#minecraft:infiniburn_end";
        c.effects = "minecraft:the_end";
        c.ambient_light = 0.0;
        c.piglin_safe = false;
        c.has_raids = true;
        c.has_skylight = false;
        c.has_ceiling = false;
        c.monster_spawn_block_light_limit = 0;
        c.monster_spawn_light_level = "7";
        return c;
    }
}
