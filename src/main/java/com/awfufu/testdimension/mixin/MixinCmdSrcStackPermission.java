package com.awfufu.testdimension.mixin;

import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CommandSourceStack.class)
public abstract class MixinCmdSrcStackPermission {

    @Inject(method = "hasPermission", at = @At("RETURN"), cancellable = true,remap = false)
    private void setBoostedPerm(int p_81370_,CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer thisPlayer = ((CommandSourceStack)(Object)this).getPlayer();
        if(thisPlayer == null) {return;}
        PlayerDimensionData data = thisPlayer.getData(ModAttachments.PLAYER_DIMENSION_DATA);
        if(data != null&& data.isInTestDimension()&&data.getSavedPermissionLevel()<2) {
            //TestDimensionMod.LOGGER.debug("Boosted permission changed for {}" , thisPlayer.getDisplayName());
            cir.setReturnValue(2>=p_81370_);
            thisPlayer.connection.send(new ClientboundEntityEventPacket(thisPlayer,(byte)(24+2)));
            return;
        }else if(data!=null){
            thisPlayer.connection.send(new ClientboundEntityEventPacket(thisPlayer,(byte)(24+data.getSavedPermissionLevel())));
            return;
        }
        TestDimensionMod.LOGGER.error("this player doesn't have permission data");
    }
}

