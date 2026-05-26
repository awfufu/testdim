package com.awfufu.testdimension.event;

import java.lang.reflect.Field;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import com.awfufu.testdimension.command.TestDimensionCommands;
import com.awfufu.testdimension.player.PlayerStateManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TestDimensionMod.MOD_ID)
public final class TestDimensionEvents {
    private TestDimensionEvents() {
    }

    private static final SimpleCommandExceptionType RESTRICTED_DIMENSION =
            new SimpleCommandExceptionType(
                    Component.literal("In the test dimension, execute in can only target testdim:test"));

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
        RedirectModifier<CommandSourceStack> originalModifier = inNode.getRedirectModifier();
        if (originalModifier == null) return;

        RedirectModifier<CommandSourceStack> wrapped = context -> {
            CommandSourceStack source = context.getSource();
            if (source.getEntity() instanceof ServerPlayer player) {
                PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
                if (data.isInTestDimension()) {
                    var redirected = originalModifier.apply(context);
                    for (CommandSourceStack rs : redirected) {
                        if (rs.getLevel().dimension() != TestDimensionKeys.TEST_WORLD) {
                            throw RESTRICTED_DIMENSION.create();
                        }
                    }
                    return redirected;
                }
            }
            return originalModifier.apply(context);
        };

        setPrivateField(CommandNode.class, inNode, "modifier", wrapped);
        TestDimensionMod.LOGGER.info("Wrapped execute/in redirect modifier for test dimension restriction");
    }

    @SuppressWarnings("unchecked")
    private static void wrapDimensionSuggestions(CommandNode<CommandSourceStack> inNode) {
        CommandNode<CommandSourceStack> dimNode = inNode.getChild("dimension");
        if (dimNode == null) return;

        com.mojang.brigadier.arguments.ArgumentType<?> argType = getPrivateField(dimNode, "type");

        SuggestionProvider<CommandSourceStack> wrappedSuggestion = (ctx, builder) -> {
            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
                if (data.isInTestDimension()) {
                    builder.suggest(TestDimensionKeys.TEST_WORLD_ID.toString());
                    return builder.buildFuture();
                }
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
    public static void onCommandEvent(CommandEvent event) {
        String cmd = event.getParseResults().getReader().getString();
        if (cmd == null || cmd.isEmpty()) return;

        if (containsNonTestDimensionTarget(cmd) && isAnyPlayerInTestDimension()) {
            event.setCanceled(true);
            TestDimensionMod.LOGGER.info("Blocked cross-dimension command: {}", cmd);
        }
    }

    private static boolean isAnyPlayerInTestDimension() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
            if (data.isInTestDimension()) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsNonTestDimensionTarget(String command) {
        int idx = 0;
        while ((idx = command.indexOf(" in ", idx)) != -1) {
            idx += 4;
            while (idx < command.length() && command.charAt(idx) == ' ') {
                idx++;
            }
            int end = idx;
            while (end < command.length() && !Character.isWhitespace(command.charAt(end))) {
                end++;
            }
            String dimArg = command.substring(idx, end).trim();
            if (!dimArg.isEmpty()) {
                net.minecraft.resources.ResourceLocation loc =
                        net.minecraft.resources.ResourceLocation.tryParse(dimArg);
                if (loc != null && !TestDimensionKeys.TEST_WORLD_ID.equals(loc)) {
                    return true;
                }
            }
            idx = end;
        }
        return false;
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
