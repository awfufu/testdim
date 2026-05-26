package com.awfufu.testdimension.network;

public interface ClientScreenHandler {
    ClientScreenHandler NOOP = new ClientScreenHandler() {
        @Override
        public void openConfigScreen() {
        }

        @Override
        public void onDatapackResponse(String typeConfigJson, String dimConfigJson,
                String dimensionId, String error) {
        }
    };

    void openConfigScreen();

    void onDatapackResponse(String typeConfigJson, String dimConfigJson,
            String dimensionId, String error);
}
