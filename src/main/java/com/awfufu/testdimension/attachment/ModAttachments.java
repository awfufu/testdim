package com.awfufu.testdimension.attachment;

import java.util.function.Supplier;

import com.awfufu.testdimension.TestDimensionMod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TestDimensionMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerDimensionData>> PLAYER_DIMENSION_DATA = ATTACHMENT_TYPES.register(
            "player_dimension_data",
            () -> AttachmentType.serializable(holder -> new PlayerDimensionData()).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
