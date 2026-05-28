package com.awfufu.testdimension.event;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import com.awfufu.testdimension.command.TestDimensionCommands;
import com.awfufu.testdimension.data.DimDataModifier;
import com.awfufu.testdimension.player.PlayerStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = TestDimensionMod.MOD_ID)
public final class TestDimensionEvents {
    private TestDimensionEvents() {
    }

    private static final SimpleCommandExceptionType RESTRICTED_DIMENSION =
            new SimpleCommandExceptionType(
                    Component.translatable("testdim.command.restricted_dimension"));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TestDimensionCommands.register(event.getDispatcher());
        applyDimensionRestrictions(event.getDispatcher());
        TestDimensionMod.LOGGER.info("Registered /testdim commands");
    }

    @SuppressWarnings("unchecked")
    private static void applyDimensionRestrictions(CommandDispatcher<CommandSourceStack> dispatcher) {
        try {
            CommandNode<CommandSourceStack> executeNode = dispatcher.getRoot().getChild("execute");
            if (executeNode == null) return;
            CommandNode<CommandSourceStack> inNode = executeNode.getChild("in");
            if (inNode == null) return;

            wrapRedirectModifier(inNode);
            wrapDimensionSuggestions(inNode);

            TestDimensionMod.LOGGER.info("Applied test dimension command restrictions on execute/in/dimension");
        } catch (Exception e) {
            TestDimensionMod.LOGGER.error("Failed to apply dimension restrictions", e);
        }
    }

    private static void wrapRedirectModifier(CommandNode<CommandSourceStack> inNode) {
        CommandNode<CommandSourceStack> dimensionNode = inNode.getChild("dimension");
        if (dimensionNode == null) return;
        RedirectModifier<CommandSourceStack> originalModifier = dimensionNode.getRedirectModifier();
        if (originalModifier == null) {
            TestDimensionMod.LOGGER.error("dimension node has no redirect modifier");
            return;
        }

        RedirectModifier<CommandSourceStack> wrapped = context -> {
            CommandSourceStack source = context.getSource();
            if (source.getLevel().dimension() == TestDimensionKeys.TEST_WORLD()) {
                var redirected = originalModifier.apply(context);
                for (CommandSourceStack rs : redirected) {
                    if (rs.getLevel().dimension() != TestDimensionKeys.TEST_WORLD()) {
                        throw RESTRICTED_DIMENSION.create();
                    }
                }
                return redirected;
            }
            return originalModifier.apply(context);
        };

        setPrivateField(CommandNode.class, dimensionNode, "modifier", wrapped);
        TestDimensionMod.LOGGER.info("Wrapped execute/in/dimension redirect modifier for test dimension restriction");
    }

    @SuppressWarnings("unchecked")
    private static void wrapDimensionSuggestions(CommandNode<CommandSourceStack> inNode) {
        CommandNode<CommandSourceStack> dimNode = inNode.getChild("dimension");
        if (dimNode == null) {
            TestDimensionMod.LOGGER.error("Failed to apply dimension suggestions");
            return;
        }

        com.mojang.brigadier.arguments.ArgumentType<?> argType = getPrivateField(dimNode, "type");

        SuggestionProvider<CommandSourceStack> wrappedSuggestion = (ctx, builder) -> {


            if (ctx.getSource().getLevel().dimension() == TestDimensionKeys.TEST_WORLD()) {
                builder.suggest(TestDimensionKeys.TEST_WORLD_ID().toString());
                return builder.buildFuture();
            }

            if (argType != null) {
                argType.listSuggestions(ctx, builder);
            }
            return builder.buildFuture();
        };

        setPrivateField(dimNode.getClass(), dimNode, "customSuggestions", wrappedSuggestion);
        TestDimensionMod.LOGGER.info("Replaced execute/in/dimension suggestion provider");
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerStateManager.handleDimensionTransition(player, event.getFrom(), event.getTo());

        if (event.getTo() == TestDimensionKeys.TEST_WORLD()) {
            TestDimensionMod.LOGGER.info("Applied test-dimension transition to {}", player.getGameProfile().getName());
            player = (ServerPlayer) event.getEntity();
            player.server.getCommands().sendCommands(player);
            player.server.getPlayerList().sendPlayerPermissionLevel(player);
        } else if (event.getFrom() == TestDimensionKeys.TEST_WORLD()) {
            TestDimensionMod.LOGGER.info("Applied normal-dimension transition to {}", player.getGameProfile().getName());
            player = (ServerPlayer) event.getEntity();
            player.server.getCommands().sendCommands(player);
            player.server.getPlayerList().sendPlayerPermissionLevel(player);
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

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        DimDataModifier.detectAndApplyDatapackOverrides(event.getServer());
        TestDimensionMod.LOGGER.info("Server started - checked for datapack dimension overrides");
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
            if (data.isRespawnInTestDimension()) {
                ServerLevel testLevel = player.server.getLevel(TestDimensionKeys.TEST_WORLD());
                if (testLevel != null) {
                    player.server.execute(() -> {
                        if (!player.isRemoved()) {
                            PlayerStateManager.teleportToTestDimensionRespawn(player, testLevel);
                        }
                    });
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object instance, String fieldName) {
        try {
            Field field = findField(instance instanceof Class<?> c ? c : instance.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(instance instanceof Class<?> ? null : instance);
        } catch (Exception e) {
            TestDimensionMod.LOGGER.warn("Failed to get field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    private static void setPrivateField(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = findField(clazz, fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            TestDimensionMod.LOGGER.warn("Failed to set field {}: {}", fieldName, e.getMessage());
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " not found in " + clazz);
    }
}
