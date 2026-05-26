package com.awfufu.testdimension.mixin;

import java.util.Collection;
import java.util.Collections;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@Mixin(CommandSourceStack.class)
public class MixinCommandSourceStack {

    @Inject(method = "levels", at = @At("HEAD"), cancellable = true)
    private void restrictLevels(CallbackInfoReturnable<Collection<ServerLevel>> cir) {
        CommandSourceStack self = (CommandSourceStack) (Object) this;
        if (self.getEntity() instanceof ServerPlayer player) {
            PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);
            if (data.isInTestDimension()) {
                ServerLevel testLevel = player.server.getLevel(TestDimensionKeys.TEST_WORLD);
                if (testLevel != null) {
                    cir.setReturnValue(Collections.singleton(testLevel));
                }
            }
        }
    }

}
