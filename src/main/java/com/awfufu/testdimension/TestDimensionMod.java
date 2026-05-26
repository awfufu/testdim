package com.awfufu.testdimension;

import org.slf4j.Logger;

import com.awfufu.testdimension.attachment.ModAttachments;
import com.awfufu.testdimension.data.DimDataModifier;
import com.awfufu.testdimension.event.TestDimensionEvents;
import com.awfufu.testdimension.network.ModNetwork;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(TestDimensionMod.MOD_ID)
public final class TestDimensionMod {
    public static final String MOD_ID = "testdimension";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TestDimensionMod(IEventBus modBus) {
        ModAttachments.register(modBus);
        ModNetwork.register(modBus);
        NeoForge.EVENT_BUS.register(TestDimensionEvents.class);
        DimDataModifier.loadTypeConfigFromFile(DimDataModifier.getDefaultTypeConfigPath());
        DimDataModifier.loadDimensionConfigFromFile(DimDataModifier.getDefaultDimensionConfigPath());
        LOGGER.info("Initializing {}", MOD_ID);
    }
}
