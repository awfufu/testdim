package com.awfufu.testdimension.command;

import static net.minecraft.commands.Commands.literal;

import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.data.DimDataModifier;
import com.awfufu.testdimension.network.ModNetwork;
import com.awfufu.testdimension.player.PlayerStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class TestDimensionCommands {
    private TestDimensionCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerRoot(dispatcher, "td");
        registerRoot(dispatcher, "testdim");
    }

    private static void registerRoot(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        dispatcher.register(literal(root)
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("enter").executes(context -> enter(context.getSource())))
                .then(Commands.literal("leave").executes(context -> leave(context.getSource())))
                .then(Commands.literal("modify").requires(source -> source.hasPermission(2))
                        .executes(context -> modify(context.getSource())))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2))
                        .executes(context -> reloadFromFiles(context.getSource()))
                        .then(Commands.argument("dimension", StringArgumentType.word())
                                .executes(context -> reloadFromDatapack(context.getSource(),
                                        StringArgumentType.getString(context, "dimension"))))));
    }

    private static int enter(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (PlayerStateManager.enterTestDimension(player, source)) {
            source.sendSuccess(() -> Component.literal("Entered test dimension."), true);
            return 1;
        }
        return 0;
    }

    private static int leave(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (PlayerStateManager.leaveTestDimension(player, source)) {
            source.sendSuccess(() -> Component.literal("Returned from the test dimension."), true);
            return 1;
        }
        return 0;
    }

    private static int modify(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ModNetwork.sendOpenScreenToPlayer(player);
        source.sendSuccess(() -> Component.literal("Opened dimension configuration screen."), true);
        return 1;
    }

    private static int reloadFromFiles(CommandSourceStack source) {
        try {
            DimDataModifier.loadTypeConfigFromFile(DimDataModifier.getDefaultTypeConfigPath());
            DimDataModifier.loadDimensionConfigFromFile(DimDataModifier.getDefaultDimensionConfigPath());
            source.sendSuccess(() -> Component.literal("Reloaded dimension config from files. Changes will take effect after server restart."), true);
            TestDimensionMod.LOGGER.info("Reloaded dimension config from files");
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload: " + e.getMessage()));
            TestDimensionMod.LOGGER.error("Failed to reload dimension config", e);
            return 0;
        }
    }

    private static int reloadFromDatapack(CommandSourceStack source, String dimensionId) {
        try {
            ResourceLocation loc = ResourceLocation.tryParse(dimensionId);
            if (loc == null) {
                source.sendFailure(Component.literal("Invalid dimension ID: " + dimensionId));
                return 0;
            }
            var server = source.getServer();
            var typeConfig = DimDataModifier.loadTypeConfigFromDatapack(server, loc);
            if (typeConfig == null) {
                source.sendFailure(Component.literal("Dimension type not found for: " + loc));
                return 0;
            }
            DimDataModifier.updateTypeConfig(typeConfig);
            var dimConfig = DimDataModifier.loadDimConfigFromDatapack(server, loc);
            if (dimConfig != null) {
                DimDataModifier.updateDimConfig(dimConfig);
            }
            DimDataModifier.saveTypeConfigToFile(DimDataModifier.getDefaultTypeConfigPath());
            DimDataModifier.saveDimensionConfigToFile(DimDataModifier.getDefaultDimensionConfigPath());
            source.sendSuccess(
                    () -> Component.literal("Loaded dimension config from " + loc + ". Changes take effect after restart."),
                    true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed: " + e.getMessage()));
            return 0;
        }
    }
}
