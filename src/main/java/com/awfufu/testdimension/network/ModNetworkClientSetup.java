package com.awfufu.testdimension.network;

import com.awfufu.testdimension.data.DimensionConfigScreen;

import net.minecraft.client.Minecraft;

public final class ModNetworkClientSetup {
    private ModNetworkClientSetup() {
    }

    public static void init() {
        ModNetwork.setScreenHandler(new ClientScreenHandler() {
            @Override
            public void openConfigScreen() {
                Minecraft.getInstance().setScreen(new DimensionConfigScreen());
            }

            @Override
            public void onDatapackResponse(String typeConfigJson, String dimConfigJson,
                    String dimensionId, String error) {
                if (Minecraft.getInstance().screen instanceof DimensionConfigScreen screen) {
                    screen.onDatapackResponse(typeConfigJson, dimConfigJson, dimensionId, error);
                }
            }
        });
    }
}
