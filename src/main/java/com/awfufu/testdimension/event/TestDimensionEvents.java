package com.awfufu.testdimension.event;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.command.TestDimensionCommands;
import com.awfufu.testdimension.player.PlayerStateManager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TestDimensionMod.MOD_ID)
public final class TestDimensionEvents {
    private TestDimensionEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TestDimensionCommands.register(event.getDispatcher());
        TestDimensionMod.LOGGER.info("Registered /testdim commands");
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerStateManager.handleDimensionTransition(player, event.getFrom(), event.getTo());

        if (event.getTo() == TestDimensionKeys.TEST_WORLD) {
            TestDimensionMod.LOGGER.info("Applied test-dimension transition to {}", player.getGameProfile().getName());
        } else if (event.getFrom() == TestDimensionKeys.TEST_WORLD) {
            TestDimensionMod.LOGGER.info("Applied normal-dimension transition to {}", player.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            PlayerStateManager.copyPersistentData(oldPlayer, newPlayer);
            if (event.isWasDeath()) {
                PlayerStateManager.reconcileAfterRespawnFromTestDimension(oldPlayer, newPlayer);
            }
            TestDimensionMod.LOGGER.info("Copied attachment data from {} to cloned player entity", oldPlayer.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStateManager.reconcileOnLogin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStateManager.saveCurrentStateIfNeeded(player);
        }
    }
}
