package com.awfufu.testdimension.attachment;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;

public final class TestDimensionGlobalData {
    private TestDimensionGlobalData() {
    }

    @Nullable
    public static ServerLevel getTestDimension(MinecraftServer server) {
        if (server == null) return null;
        return server.getLevel(TestDimensionKeys.TEST_WORLD());
    }

    @Nullable
    public static GameRules getGameRules(MinecraftServer server) {
        ServerLevel level = getTestDimension(server);
        if (level == null) {
            TestDimensionMod.LOGGER.warn("Test dimension not loaded, cannot access gamerules");
            return null;
        }
        return level.getGameRules();
    }

    public static <T extends GameRules.Value<T>> T getRule(MinecraftServer server, GameRules.Key<T> key) {
        GameRules rules = getGameRules(server);
        return rules != null ? rules.getRule(key) : null;
    }

    public static boolean getBoolean(MinecraftServer server, GameRules.Key<GameRules.BooleanValue> key) {
        GameRules rules = getGameRules(server);
        return rules != null && rules.getBoolean(key);
    }

    public static int getInt(MinecraftServer server, GameRules.Key<GameRules.IntegerValue> key) {
        GameRules rules = getGameRules(server);
        return rules != null ? rules.getInt(key) : 0;
    }

    public static boolean isDimensionLoaded(MinecraftServer server) {
        return getTestDimension(server) != null;
    }
}
