package com.awfufu.testdimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class TestDimensionKeys {
    private static ResourceLocation testWorldId = ResourceLocation.fromNamespaceAndPath("testdim", "test");
    private static ResourceLocation testWorldTypeId = ResourceLocation.fromNamespaceAndPath("testdim", "test_type");

    private static ResourceKey<Level> testWorld = ResourceKey.create(Registries.DIMENSION, testWorldId);
    private static ResourceKey<DimensionType> testWorldType = ResourceKey.create(Registries.DIMENSION_TYPE, testWorldTypeId);

    public static ResourceLocation TEST_WORLD_ID() {
        return testWorldId;
    }

    public static ResourceLocation TEST_WORLD_TYPE_ID() {
        return testWorldTypeId;
    }

    public static ResourceKey<Level> TEST_WORLD() {
        return testWorld;
    }

    public static ResourceKey<DimensionType> TEST_WORLD_TYPE() {
        return testWorldType;
    }

    public static void overrideKeys(ResourceLocation dimensionId, ResourceLocation typeId) {
        testWorldId = dimensionId;
        testWorldTypeId = typeId;
        testWorld = ResourceKey.create(Registries.DIMENSION, dimensionId);
        testWorldType = ResourceKey.create(Registries.DIMENSION_TYPE, typeId);
    }

    public static void overrideDimensionKey(ResourceLocation dimensionId) {
        testWorldId = dimensionId;
        testWorld = ResourceKey.create(Registries.DIMENSION, dimensionId);
    }

    public static void overrideTypeKey(ResourceLocation typeId) {
        testWorldTypeId = typeId;
        testWorldType = ResourceKey.create(Registries.DIMENSION_TYPE, typeId);
    }

    private TestDimensionKeys() {
    }
}
