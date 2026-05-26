package com.awfufu.testdimension.mixin;

import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinPlayerPermission {
    @Inject(method = "getProfilePermissions", at = @At("HEAD"), cancellable = true)
    private void setBoostedPerm(GameProfile p_129945_, CallbackInfoReturnable<Integer> cir) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer Inst = (MinecraftServer)(Object)this;
        if(mc.level == null||Inst == null) return;
        Player currentPlayer = mc.level.getPlayerByUUID(p_129945_.getId());
        if(currentPlayer == null) return;
        PlayerDimensionData data = currentPlayer.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if(data == null) return;
        if (data.isInTestDimension()&&!Inst.getPlayerList().isOp(p_129945_)) {
            cir.setReturnValue(2);
        }
    }
}

