package com.awfufu.testdimension.attachment;

import com.awfufu.testdimension.player.PlayerStateProfile;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class PlayerDimensionData implements INBTSerializable<CompoundTag> {
    private PlayerStateProfile normalProfile = PlayerStateProfile.empty();
    private PlayerStateProfile testProfile = PlayerStateProfile.empty();
    private boolean inTestDimension;
    private boolean testProfileInitialized;
    private transient boolean switching;

    private int savedPermissionLevel = -1;
    private boolean permissionOverridden;

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

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("normal_profile", this.normalProfile.toTag());
        tag.put("test_profile", this.testProfile.toTag());
        tag.putBoolean("in_test_dimension", this.inTestDimension);
        tag.putBoolean("test_profile_initialized", this.testProfileInitialized);
        tag.putInt("saved_permission_level", this.savedPermissionLevel);
        tag.putBoolean("permission_overridden", this.permissionOverridden);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.normalProfile = PlayerStateProfile.fromTag(tag.getCompound("normal_profile"));
        this.testProfile = PlayerStateProfile.fromTag(tag.getCompound("test_profile"));
        this.inTestDimension = tag.getBoolean("in_test_dimension");
        this.testProfileInitialized = tag.getBoolean("test_profile_initialized");
        this.switching = false;
        this.savedPermissionLevel = tag.getInt("saved_permission_level");
        this.permissionOverridden = tag.getBoolean("permission_overridden");
    }
}
