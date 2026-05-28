package com.awfufu.testdimension.attachment;

import com.awfufu.testdimension.TestDimensionKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;

public final class TestDimensionGlobalData {
    private static final GameRules testDimensionGameRules = new GameRules();

    private TestDimensionGlobalData() {
    }

    public static GameRules getGameRules() {
        return testDimensionGameRules;
    }

    @Nullable
    public static ServerLevel getTestDimension(MinecraftServer server) {
        if (server == null) return null;
        return server.getLevel(TestDimensionKeys.TEST_WORLD());
    }

    public static boolean isDimensionLoaded(MinecraftServer server) {
        return getTestDimension(server) != null;
    }

    public static boolean getBoolean(GameRules.Key<GameRules.BooleanValue> key) {
        return testDimensionGameRules.getBoolean(key);
    }

    public static int getInt(GameRules.Key<GameRules.IntegerValue> key) {
        return testDimensionGameRules.getInt(key);
    }

    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> key) {
        return testDimensionGameRules.getRule(key);
    }
}
