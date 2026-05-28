package com.awfufu.testdimension.mixin;

import com.awfufu.testdimension.TestDimensionKeys;
import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.attachment.TestDimensionGlobalData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Level.class)
public class MixinGameRules {
    @Inject(method = "getGameRules",at=@At("RETURN"),cancellable = true, remap = false)
    private void getIsolatedGameRules(CallbackInfoReturnable<GameRules> cir) {
        Level thisLevel = (Level)(Object)this;
        if(Objects.equals(thisLevel.dimension(), TestDimensionKeys.TEST_WORLD())){
            cir.setReturnValue(TestDimensionGlobalData.getGameRules());
        }
    }
}
