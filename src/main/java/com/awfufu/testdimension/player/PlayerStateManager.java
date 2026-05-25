package com.awfufu.testdimension.player;

import java.util.Set;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PlayerStateManager {
    private PlayerStateManager() {
    }

    public static boolean enterTestDimension(ServerPlayer player, CommandSourceStack source) {
        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        boolean inTestDimension = isInTestDimension(player);
        if (data.isSwitching()) {
            source.sendFailure(Component.literal("Player state is already switching."));
            return false;
        }

        ServerLevel targetLevel = player.server.getLevel(TestDimensionKeys.TEST_WORLD);
        if (targetLevel == null) {
            TestDimensionMod.LOGGER.error("Test dimension {} is not available", TestDimensionKeys.TEST_WORLD.location());
            source.sendFailure(Component.literal("Test dimension is not available."));
            return false;
        }

        if (inTestDimension) {
            source.sendSuccess(() -> Component.literal("Already in the test dimension."), false);
            return true;
        }

        data.setNormalProfile(capturePlayerState(player));
        if (!data.isTestProfileInitialized()) {
            PlayerStateProfile initialTestProfile = PlayerStateProfile.createDefaultTestProfile();
            data.setTestProfile(initialTestProfile);
            data.setTestProfileInitialized(true);
            TestDimensionMod.LOGGER.info("Initialized test profile for {}", player.getGameProfile().getName());
        }

        if (isPositionInTestDimension(data.testProfile().position())) {
            teleportToSavedOrFallback(player, data.testProfile().position());
        }

        if (player.level().dimension() != TestDimensionKeys.TEST_WORLD) {
            teleportToLevelSpawn(player, targetLevel);
        }

        TestDimensionMod.LOGGER.info("Saved normal profile and started test-dimension transfer for {}", player.getGameProfile().getName());
        return true;
    }

    public static boolean leaveTestDimension(ServerPlayer player, CommandSourceStack source) {
        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        boolean inTestDimension = isInTestDimension(player);
        if (data.isSwitching()) {
            source.sendFailure(Component.literal("Player state is already switching."));
            return false;
        }

        if (!inTestDimension) {
            source.sendSuccess(() -> Component.literal("Player is not in the test dimension."), false);
            return true;
        }

        data.setTestProfile(capturePlayerState(player));
        teleportToSavedOrFallback(player, data.normalProfile().position());
        TestDimensionMod.LOGGER.info("Saved test profile and started normal-dimension transfer for {}", player.getGameProfile().getName());
        return true;
    }

    public static void handleDimensionTransition(ServerPlayer player, net.minecraft.resources.ResourceKey<Level> from, net.minecraft.resources.ResourceKey<Level> to) {
        if (!isTestTransition(from, to)) {
            return;
        }

        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if (data.isSwitching()) {
            return;
        }

        data.setSwitching(true);
        try {
            if (to == TestDimensionKeys.TEST_WORLD) {
                if (!data.isTestProfileInitialized()) {
                    PlayerStateProfile initialTestProfile = PlayerStateProfile.createDefaultTestProfile();
                    data.setTestProfile(initialTestProfile);
                    data.setTestProfileInitialized(true);
                    TestDimensionMod.LOGGER.info("Initialized test profile for {}", player.getGameProfile().getName());
                }

                applyPlayerState(player, data.testProfile());
                data.setInTestDimension(true);
                TestDimensionMod.LOGGER.info("Restored test profile for {} after dimension change", player.getGameProfile().getName());
                return;
            }

            if (from == TestDimensionKeys.TEST_WORLD) {
                applyPlayerState(player, data.normalProfile());
                data.setInTestDimension(false);
                TestDimensionMod.LOGGER.info("Restored normal profile for {} after leaving test dimension", player.getGameProfile().getName());
            }
        } finally {
            data.setSwitching(false);
        }
    }

    public static PlayerStateProfile capturePlayerState(ServerPlayer player) {
        ListTag inventory = player.getInventory().save(new ListTag());
        ListTag enderChest = player.getEnderChestInventory().createTag(player.registryAccess());
        return new PlayerStateProfile(
                inventory.copy(),
                enderChest.copy(),
                player.experienceLevel,
                player.experienceProgress,
                player.totalExperience,
                player.getHealth(),
                player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel(),
                gameModeName(player.gameMode.getGameModeForPlayer()),
                captureCurrentPosition(player));
    }

    public static void applyPlayerState(ServerPlayer player, PlayerStateProfile profile) {
        SavedPosition targetPosition = profile.position();
        player.getInventory().load(profile.inventory().copy());
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        enderChest.fromTag(profile.enderChest().copy(), player.registryAccess());

        player.totalExperience = profile.totalExperience();
        player.setExperienceLevels(profile.experienceLevel());
        int pointsForCurrentLevel = Math.round(profile.experienceProgress() * player.getXpNeededForNextLevel());
        player.setExperiencePoints(pointsForCurrentLevel);

        player.getFoodData().setFoodLevel(profile.foodLevel());
        player.getFoodData().setSaturation(profile.saturation());
        player.setHealth(Math.min(profile.health(), player.getMaxHealth()));
        player.setGameMode(parseGameMode(profile.gameMode()));

        player.inventoryMenu.slotsChanged(player.getInventory());
        player.containerMenu.broadcastChanges();
        player.initInventoryMenu();

        if(isInTestDimension(player)&&player.isDeadOrDying()) {
            player.setHealth(player.getMaxHealth());
        }

        player.respawn();

        if (targetPosition != null) {
            teleportToSavedOrFallback(player, targetPosition);
        } else if (player.level().dimension() == TestDimensionKeys.TEST_WORLD) {
            ServerLevel targetLevel = player.server.getLevel(TestDimensionKeys.TEST_WORLD);
            if (targetLevel != null) {
                teleportToLevelSpawn(player, targetLevel);
            }
        }
    }

    public static SavedPosition captureCurrentPosition(ServerPlayer player) {
        return new SavedPosition(
                player.level().dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot());
    }

    public static void saveCurrentStateIfNeeded(ServerPlayer player) {
        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if (data.isSwitching()) {
            return;
        }

        if (isInTestDimension(player)) {
            data.setTestProfile(capturePlayerState(player));
            data.setInTestDimension(true);
            TestDimensionMod.LOGGER.info("Saved test profile for {} during lifecycle event", player.getGameProfile().getName());
        } else {
            data.setNormalProfile(capturePlayerState(player));
            data.setInTestDimension(false);
            TestDimensionMod.LOGGER.info("Saved normal profile for {} during lifecycle event", player.getGameProfile().getName());
        }
    }

    public static void reconcileOnLogin(ServerPlayer player) {
        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if (data.isSwitching()) {
            return;
        }

        boolean actuallyInTestDimension = isInTestDimension(player);
        boolean storedInTestDimension = data.isInTestDimension();
        if (actuallyInTestDimension) {
            data.setTestProfile(capturePlayerState(player));
            data.setTestProfileInitialized(true);
            data.setInTestDimension(true);
        } else {
            data.setNormalProfile(capturePlayerState(player));
            data.setInTestDimension(false);
        }

        if (storedInTestDimension != actuallyInTestDimension) {
            TestDimensionMod.LOGGER.warn(
                    "Player {} test dimension flag mismatch: stored={}, actual={}",
                    player.getGameProfile().getName(),
                    storedInTestDimension,
                    actuallyInTestDimension);
            data.setInTestDimension(actuallyInTestDimension);
        }
    }

    public static void copyPersistentData(ServerPlayer from, ServerPlayer to) {
        PlayerDimensionData source = from.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        PlayerDimensionData target = to.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        target.setNormalProfile(source.normalProfile());
        target.setTestProfile(source.testProfile());
        target.setInTestDimension(source.isInTestDimension());
        target.setTestProfileInitialized(source.isTestProfileInitialized());
        target.setSwitching(false);
    }

    public static void reconcileAfterRespawnFromTestDimension(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        PlayerDimensionData data = newPlayer.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if (data.isSwitching() || oldPlayer.level().dimension() != TestDimensionKeys.TEST_WORLD) {
            return;
        }

        data.setTestProfile(capturePlayerState(oldPlayer));
        applyPlayerState(newPlayer, data.normalProfile());
        data.setInTestDimension(false);
        TestDimensionMod.LOGGER.info(
                "Restored normal profile for {} after respawning from the test dimension",
                newPlayer.getGameProfile().getName());
    }

    private static void teleportToLevelSpawn(ServerPlayer player, ServerLevel targetLevel) {
        if (targetLevel.dimension() == TestDimensionKeys.TEST_WORLD) {
            double x = 0.5D;
            double y = 64.0D;
            double z = 0.5D;
            player.teleportTo(targetLevel, x, y, z, Set.<RelativeMovement>of(), player.getYRot(), player.getXRot());
            return;
        }

        BlockPos targetPos = targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetLevel.getSharedSpawnPos());
        double x = targetPos.getX() + 0.5D;
        double y = targetPos.getY() + 1.0D;
        double z = targetPos.getZ() + 0.5D;
        player.teleportTo(targetLevel, x, y, z, Set.<RelativeMovement>of(), player.getYRot(), player.getXRot());
    }

    private static void teleportToSavedOrFallback(ServerPlayer player, SavedPosition savedPosition) {
        MinecraftServer server = player.server;
        if (savedPosition != null) {
            ResourceLocation dimensionId = ResourceLocation.tryParse(savedPosition.dimension());
            if (dimensionId != null) {
                ServerLevel savedLevel = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId));
                if (savedLevel != null) {
                    player.teleportTo(savedLevel, savedPosition.x(), savedPosition.y(), savedPosition.z(), Set.<RelativeMovement>of(), savedPosition.yaw(), savedPosition.pitch());
                    return;
                }
            }
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available");
        }
        teleportToLevelSpawn(player, overworld);
    }

    private static String gameModeName(GameType gameType) {
        return switch (gameType) {
            case CREATIVE -> "creative";
            case ADVENTURE -> "adventure";
            case SPECTATOR -> "spectator";
            default -> "survival";
        };
    }

    private static GameType parseGameMode(String name) {
        return switch (name) {
            case "creative" -> GameType.CREATIVE;
            case "adventure" -> GameType.ADVENTURE;
            case "spectator" -> GameType.SPECTATOR;
            default -> GameType.SURVIVAL;
        };
    }

    private static boolean isInTestDimension(ServerPlayer player) {
        return player.level().dimension() == TestDimensionKeys.TEST_WORLD;
    }

    private static boolean isTestTransition(net.minecraft.resources.ResourceKey<Level> from, net.minecraft.resources.ResourceKey<Level> to) {
        return from == TestDimensionKeys.TEST_WORLD || to == TestDimensionKeys.TEST_WORLD;
    }

    private static boolean isPositionInTestDimension(SavedPosition savedPosition) {
        return savedPosition != null && TestDimensionKeys.TEST_WORLD.location().toString().equals(savedPosition.dimension());
    }
}
