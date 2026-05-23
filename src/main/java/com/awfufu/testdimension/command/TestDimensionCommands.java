package com.awfufu.testdimension.command;

import static net.minecraft.commands.Commands.literal;

import com.awfufu.testdimension.player.PlayerStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("enter").executes(context -> enter(context.getSource())))
                .then(Commands.literal("leave").executes(context -> leave(context.getSource()))));
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
}
