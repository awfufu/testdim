package com.awfufu.testdimension.mixin;

import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class MixinPlayerPermission {
    @Inject(method = "getPermissionLevel", at = @At("RETURN"), cancellable = true,remap = false)
    private void setBoostedPerm(CallbackInfoReturnable<Integer> cir) {
        ServerPlayer thisPlayer = (ServerPlayer)(Object)this;
        PlayerDimensionData data = thisPlayer.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        TestDimensionMod.LOGGER.warn("Boosted permission changed for {}" , thisPlayer.getDisplayName());
        if(data != null&& data.isInTestDimension()) {
            cir.setReturnValue(2);
        }else{
            TestDimensionMod.LOGGER.warn("Boosted failed for {}" , thisPlayer.getDisplayName());
            TestDimensionMod.LOGGER.warn("Because {}", data==null?"data is null":"player still not in dimension yet");
        }
    }
}

