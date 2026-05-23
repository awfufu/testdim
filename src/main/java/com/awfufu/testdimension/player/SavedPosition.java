package com.awfufu.testdimension.player;

import net.minecraft.nbt.CompoundTag;

public record SavedPosition(String dimension, double x, double y, double z, float yaw, float pitch) {
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", this.dimension);
        tag.putDouble("x", this.x);
        tag.putDouble("y", this.y);
        tag.putDouble("z", this.z);
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        return tag;
    }

    public static SavedPosition fromTag(CompoundTag tag) {
        return new SavedPosition(
                tag.getString("dimension"),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getFloat("yaw"),
                tag.getFloat("pitch"));
    }
}
