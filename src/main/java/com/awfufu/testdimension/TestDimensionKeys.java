package com.awfufu.testdimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class TestDimensionKeys {
    public static final ResourceLocation TEST_WORLD_ID = ResourceLocation.fromNamespaceAndPath("testdim", "test");
    public static final ResourceLocation TEST_WORLD_TYPE_ID = ResourceLocation.fromNamespaceAndPath("testdim", "test_type");

    public static final ResourceKey<Level> TEST_WORLD = ResourceKey.create(Registries.DIMENSION, TEST_WORLD_ID);
    public static final ResourceKey<DimensionType> TEST_WORLD_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, TEST_WORLD_TYPE_ID);

    private TestDimensionKeys() {
    }
}
