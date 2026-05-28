package com.awfufu.testdimension.mixin;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.attachment.PlayerDimensionData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerPlayer.class)
public class MixinPlayerRespawn {
    @Inject(method = "setRespawnPosition", at = @At("TAIL"), remap = false)
    private void onSetSpawn(
            ResourceKey<Level> dimension,
            @Nullable BlockPos pos,
            float angle,
            boolean forced,
            boolean sendMessage,
            CallbackInfo ci
    ) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        PlayerDimensionData data = player.getData(ModAttachments.PLAYER_DIMENSION_DATA);

        if (dimension == TestDimensionKeys.TEST_WORLD()) {
            data.setTestRespawn(pos, dimension, angle, forced);
            TestDimensionMod.LOGGER.debug("{} set test-dim spawn at {} (forced={})",
                    player.getGameProfile().getName(), pos, forced);
        } else {
            data.setNormalRespawn(pos, dimension, angle, forced);
            TestDimensionMod.LOGGER.debug("{} set normal-world spawn at {} in {} (forced={})",
                    player.getGameProfile().getName(), pos, dimension.location(), forced);
        }
    }
}
