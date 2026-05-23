package com.awfufu.testdimension.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public record PlayerStateProfile(
        ListTag inventory,
        ListTag enderChest,
        int experienceLevel,
        float experienceProgress,
        int totalExperience,
        float health,
        int foodLevel,
        float saturation,
        String gameMode,
        SavedPosition position) {

    public static PlayerStateProfile empty() {
        return new PlayerStateProfile(new ListTag(), new ListTag(), 0, 0.0F, 0, 20.0F, 20, 5.0F, "survival", null);
    }

    public static PlayerStateProfile createDefaultTestProfile() {
        return new PlayerStateProfile(new ListTag(), new ListTag(), 0, 0.0F, 0, 20.0F, 20, 5.0F, "creative", new SavedPosition("testdim:test", 0.5D, 64.0D, 0.5D, 0.0F, 0.0F));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", this.inventory.copy());
        tag.put("ender_chest", this.enderChest.copy());
        tag.putInt("experience_level", this.experienceLevel);
        tag.putFloat("experience_progress", this.experienceProgress);
        tag.putInt("total_experience", this.totalExperience);
        tag.putFloat("health", this.health);
        tag.putInt("food_level", this.foodLevel);
        tag.putFloat("saturation", this.saturation);
        tag.putString("game_mode", this.gameMode);
        if (this.position != null) {
            tag.put("position", this.position.toTag());
        }
        return tag;
    }

    public static PlayerStateProfile fromTag(CompoundTag tag) {
        if (tag.isEmpty()) {
            return empty();
        }

        return new PlayerStateProfile(
                tag.getList("inventory", CompoundTag.TAG_COMPOUND).copy(),
                tag.getList("ender_chest", CompoundTag.TAG_COMPOUND).copy(),
                tag.getInt("experience_level"),
                tag.getFloat("experience_progress"),
                tag.getInt("total_experience"),
                tag.contains("health") ? tag.getFloat("health") : 20.0F,
                tag.contains("food_level") ? tag.getInt("food_level") : 20,
                tag.contains("saturation") ? tag.getFloat("saturation") : 5.0F,
                tag.contains("game_mode") ? tag.getString("game_mode") : "survival",
                tag.contains("position") ? SavedPosition.fromTag(tag.getCompound("position")) : null);
    }
}
