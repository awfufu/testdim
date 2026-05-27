package com.awfufu.testdimension.attachment;

import com.awfufu.testdimension.player.PlayerStateProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public final class PlayerDimensionData implements INBTSerializable<CompoundTag> {
    private PlayerStateProfile normalProfile = PlayerStateProfile.empty();
    private PlayerStateProfile testProfile = PlayerStateProfile.empty();
    private boolean inTestDimension;
    private boolean testProfileInitialized;
    private transient boolean switching;
    private transient boolean respawnInTestDimension;

    private int savedPermissionLevel = -1;
    private boolean permissionOverridden;

    @Nullable
    private BlockPos normalRespawnPos;
    private ResourceKey<Level> normalRespawnDim = Level.OVERWORLD;
    private float normalRespawnAngle;
    private boolean normalRespawnForced;

    @Nullable
    private BlockPos testRespawnPos;
    private ResourceKey<Level> testRespawnDim = Level.OVERWORLD;
    private float testRespawnAngle;
    private boolean testRespawnForced;

    private boolean normalRespawnSaved;

    public PlayerStateProfile normalProfile() {
        return this.normalProfile;
    }

    public void setNormalProfile(PlayerStateProfile normalProfile) {
        this.normalProfile = normalProfile;
    }

    public PlayerStateProfile testProfile() {
        return this.testProfile;
    }

    public void setTestProfile(PlayerStateProfile testProfile) {
        this.testProfile = testProfile;
    }

    public boolean isInTestDimension() {
        return this.inTestDimension;
    }

    public void setInTestDimension(boolean inTestDimension) {
        this.inTestDimension = inTestDimension;
    }

    public boolean isTestProfileInitialized() {
        return this.testProfileInitialized;
    }

    public void setTestProfileInitialized(boolean testProfileInitialized) {
        this.testProfileInitialized = testProfileInitialized;
    }

    public boolean isSwitching() {
        return this.switching;
    }

    public void setSwitching(boolean switching) {
        this.switching = switching;
    }

    public boolean isRespawnInTestDimension() {
        return this.respawnInTestDimension;
    }

    public void setRespawnInTestDimension(boolean respawnInTestDimension) {
        this.respawnInTestDimension = respawnInTestDimension;
    }

    public int getSavedPermissionLevel() {
        return this.savedPermissionLevel;
    }

    public void setSavedPermissionLevel(int savedPermissionLevel) {
        this.savedPermissionLevel = savedPermissionLevel;
    }

    public boolean isPermissionOverridden() {
        return this.permissionOverridden;
    }

    public void setPermissionOverridden(boolean permissionOverridden) {
        this.permissionOverridden = permissionOverridden;
    }

    @Nullable
    public BlockPos getNormalRespawnPos() { return normalRespawnPos; }
    public ResourceKey<Level> getNormalRespawnDim() { return normalRespawnDim; }
    public float getNormalRespawnAngle() { return normalRespawnAngle; }
    public boolean isNormalRespawnForced() { return normalRespawnForced; }

    public void setNormalRespawn(@Nullable BlockPos pos, ResourceKey<Level> dim, float angle, boolean forced) {
        this.normalRespawnPos = pos;
        this.normalRespawnDim = dim;
        this.normalRespawnAngle = angle;
        this.normalRespawnForced = forced;
        this.normalRespawnSaved = true;
    }

    @Nullable
    public BlockPos getTestRespawnPos() { return testRespawnPos; }
    public ResourceKey<Level> getTestRespawnDim() { return testRespawnDim; }
    public float getTestRespawnAngle() { return testRespawnAngle; }
    public boolean isTestRespawnForced() { return testRespawnForced; }

    public void setTestRespawn(@Nullable BlockPos pos, ResourceKey<Level> dim, float angle, boolean forced) {
        this.testRespawnPos = pos;
        this.testRespawnDim = dim;
        this.testRespawnAngle = angle;
        this.testRespawnForced = forced;
    }

    public boolean isNormalRespawnSaved() { return normalRespawnSaved; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("normal_profile", this.normalProfile.toTag());
        tag.put("test_profile", this.testProfile.toTag());
        tag.putBoolean("in_test_dimension", this.inTestDimension);
        tag.putBoolean("test_profile_initialized", this.testProfileInitialized);
        tag.putInt("saved_permission_level", this.savedPermissionLevel);
        tag.putBoolean("permission_overridden", this.permissionOverridden);

        if (normalRespawnPos != null) {
            tag.putInt("normal_spawn_x", normalRespawnPos.getX());
            tag.putInt("normal_spawn_y", normalRespawnPos.getY());
            tag.putInt("normal_spawn_z", normalRespawnPos.getZ());
            tag.putString("normal_spawn_dim", normalRespawnDim.location().toString());
            tag.putFloat("normal_spawn_angle", normalRespawnAngle);
            tag.putBoolean("normal_spawn_forced", normalRespawnForced);
            tag.putBoolean("normal_respawn_saved", normalRespawnSaved);
        }

        if (testRespawnPos != null) {
            tag.putInt("test_spawn_x", testRespawnPos.getX());
            tag.putInt("test_spawn_y", testRespawnPos.getY());
            tag.putInt("test_spawn_z", testRespawnPos.getZ());
            tag.putString("test_spawn_dim", testRespawnDim.location().toString());
            tag.putFloat("test_spawn_angle", testRespawnAngle);
            tag.putBoolean("test_spawn_forced", testRespawnForced);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.normalProfile = PlayerStateProfile.fromTag(tag.getCompound("normal_profile"));
        this.testProfile = PlayerStateProfile.fromTag(tag.getCompound("test_profile"));
        this.inTestDimension = tag.getBoolean("in_test_dimension");
        this.testProfileInitialized = tag.getBoolean("test_profile_initialized");
        this.switching = false;
        this.respawnInTestDimension = false;
        this.savedPermissionLevel = tag.getInt("saved_permission_level");
        this.permissionOverridden = tag.getBoolean("permission_overridden");

        if (tag.contains("normal_spawn_x")) {
            normalRespawnPos = new BlockPos(tag.getInt("normal_spawn_x"), tag.getInt("normal_spawn_y"), tag.getInt("normal_spawn_z"));
            normalRespawnDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("normal_spawn_dim")));
            normalRespawnAngle = tag.getFloat("normal_spawn_angle");
            normalRespawnForced = tag.getBoolean("normal_spawn_forced");
            normalRespawnSaved = tag.getBoolean("normal_respawn_saved");
        }

        if (tag.contains("test_spawn_x")) {
            testRespawnPos = new BlockPos(tag.getInt("test_spawn_x"), tag.getInt("test_spawn_y"), tag.getInt("test_spawn_z"));
            testRespawnDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("test_spawn_dim")));
            testRespawnAngle = tag.getFloat("test_spawn_angle");
            testRespawnForced = tag.getBoolean("test_spawn_forced");
        }
    }
}
