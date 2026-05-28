package com.awfufu.testdimension.mixin;


import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.attachment.TestDimensionGlobalData;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class MixinGameRuleCommand {
    @Inject(method = "setRule",at=@At("HEAD"),cancellable = true,remap = false)
    private static <T extends GameRules.Value<T>> void setIsolatedGameRule(CommandContext<CommandSourceStack> p_137755_, GameRules.Key<T> p_137756_, CallbackInfoReturnable<Integer> cir){
        Level theLevel =p_137755_.getSource().getLevel();
        if(theLevel.dimension() == TestDimensionKeys.TEST_WORLD()){
            T t = TestDimensionGlobalData.getGameRules().getRule(p_137756_);
            t.setFromArgument(p_137755_, "value");
            p_137755_.getSource().sendSuccess(() -> Component.translatable("commands.gamerule.set", p_137756_.getId(), t.toString()), true);
             cir.setReturnValue(t.getCommandResult());
        }

    }

    @Inject(method = "queryRule",at=@At("HEAD"),cancellable = true,remap = false)
    private static <T extends GameRules.Value<T>> void getIsolatedGameRule(CommandSourceStack p_137758_, GameRules.Key<T> p_137759_, CallbackInfoReturnable<Integer> cir){
        Level theLevel =p_137758_.getLevel();
        if(theLevel.dimension() == TestDimensionKeys.TEST_WORLD()){
            T t = TestDimensionGlobalData.getGameRules().getRule(p_137759_);
            p_137758_.sendSuccess(() -> Component.translatable("commands.gamerule.query", p_137759_.getId(), t.toString()), false);
            cir.setReturnValue(t.getCommandResult());
        }
    }
}
