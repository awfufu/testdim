package com.awfufu.testdimension.network;

import com.awfufu.testdimension.TestDimensionMod;
import com.awfufu.testdimension.data.DimDataModifier;
import com.awfufu.testdimension.data.DimensionConfigScreen;
import com.awfufu.testdimension.data.DimensionGeneratorConfig;
import com.awfufu.testdimension.data.DimensionTypeConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(OpenDimConfigScreenPayload.TYPE, OpenDimConfigScreenPayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> {
                        Minecraft.getInstance().setScreen(new DimensionConfigScreen());
                    }));

            registrar.playToServer(ApplyDimConfigPayload.TYPE, ApplyDimConfigPayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> {
                        if (!(context.player() instanceof ServerPlayer player)) return;
                        if (!player.hasPermissions(2)) {
                            player.sendSystemMessage(Component.literal("Insufficient permissions."));
                            return;
                        }
                        try {
                            DimensionTypeConfig typeConfig = DimensionTypeConfig.fromJson(payload.typeConfigJson());
                            DimensionGeneratorConfig dimConfig = payload.dimConfigJson().isEmpty()
                                    ? new DimensionGeneratorConfig()
                                    : DimensionGeneratorConfig.fromJson(payload.dimConfigJson());
                            DimDataModifier.updateTypeConfig(typeConfig);
                            DimDataModifier.updateDimConfig(dimConfig);
                            DimDataModifier.saveTypeConfigToFile(DimDataModifier.getDefaultTypeConfigPath());
                            DimDataModifier.saveDimensionConfigToFile(DimDataModifier.getDefaultDimensionConfigPath());
                            player.sendSystemMessage(
                                    Component.literal("Dimension configuration applied and saved. Changes take effect after server restart."));
                            TestDimensionMod.LOGGER.info("{} applied new dimension config", player.getGameProfile().getName());
                        } catch (Exception e) {
                            player.sendSystemMessage(Component.literal("Failed to apply config: " + e.getMessage()));
                            TestDimensionMod.LOGGER.error("Failed to apply dimension config", e);
                        }
                    }));

            registrar.playToServer(RequestDatapackDimPayload.TYPE, RequestDatapackDimPayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> {
                        if (!(context.player() instanceof ServerPlayer player)) return;
                        MinecraftServer server = player.getServer();
                        if (server == null) return;
                        ResourceLocation loc = payload.dimensionId();
                        try {
                            DimensionTypeConfig typeConfig = DimDataModifier.loadTypeConfigFromDatapack(server, loc);
                            if (typeConfig == null) {
                                PacketDistributor.sendToPlayer(player,
                                        new DatapackDimResponsePayload("", "", loc.toString(),
                                                "Dimension type not found for: " + loc));
                                return;
                            }
                            DimensionGeneratorConfig dimConfig = DimDataModifier.loadDimConfigFromDatapack(server, loc);
                            if (dimConfig == null) {
                                dimConfig = new DimensionGeneratorConfig();
                            }
                            PacketDistributor.sendToPlayer(player, new DatapackDimResponsePayload(
                                    typeConfig.toJson(), dimConfig.toPrettyJson(), loc.toString(), ""));
                        } catch (Exception e) {
                            PacketDistributor.sendToPlayer(player,
                                    new DatapackDimResponsePayload("", "", loc.toString(), e.getMessage()));
                        }
                    }));

            registrar.playToClient(DatapackDimResponsePayload.TYPE, DatapackDimResponsePayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> {
                        if (Minecraft.getInstance().screen instanceof DimensionConfigScreen screen) {
                            screen.onDatapackResponse(payload.typeConfigJson(), payload.dimConfigJson(),
                                    payload.dimensionId(), payload.error());
                        }
                    }));
        });
    }

    public static void sendOpenScreenToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OpenDimConfigScreenPayload());
    }

    public record OpenDimConfigScreenPayload() implements CustomPacketPayload {
        public static final Type<OpenDimConfigScreenPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TestDimensionMod.MOD_ID, "open_dim_config_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenDimConfigScreenPayload> STREAM_CODEC = StreamCodec
                .unit(new OpenDimConfigScreenPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ApplyDimConfigPayload(String typeConfigJson, String dimConfigJson) implements CustomPacketPayload {
        public static final Type<ApplyDimConfigPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TestDimensionMod.MOD_ID, "apply_dim_config"));
        public static final StreamCodec<FriendlyByteBuf, ApplyDimConfigPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ApplyDimConfigPayload::typeConfigJson,
                ByteBufCodecs.STRING_UTF8, ApplyDimConfigPayload::dimConfigJson,
                ApplyDimConfigPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestDatapackDimPayload(ResourceLocation dimensionId) implements CustomPacketPayload {
        public static final Type<RequestDatapackDimPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TestDimensionMod.MOD_ID, "request_datapack_dim"));
        public static final StreamCodec<FriendlyByteBuf, RequestDatapackDimPayload> STREAM_CODEC = StreamCodec
                .composite(
                        ResourceLocation.STREAM_CODEC, RequestDatapackDimPayload::dimensionId,
                        RequestDatapackDimPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DatapackDimResponsePayload(String typeConfigJson, String dimConfigJson, String dimensionId,
            String error) implements CustomPacketPayload {
        public static final Type<DatapackDimResponsePayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(TestDimensionMod.MOD_ID, "datapack_dim_response"));
        public static final StreamCodec<FriendlyByteBuf, DatapackDimResponsePayload> STREAM_CODEC = StreamCodec
                .composite(
                        ByteBufCodecs.STRING_UTF8, DatapackDimResponsePayload::typeConfigJson,
                        ByteBufCodecs.STRING_UTF8, DatapackDimResponsePayload::dimConfigJson,
                        ByteBufCodecs.STRING_UTF8, DatapackDimResponsePayload::dimensionId,
                        ByteBufCodecs.STRING_UTF8, DatapackDimResponsePayload::error,
                        DatapackDimResponsePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
