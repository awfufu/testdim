package com.awfufu.testdimension.data;

import com.awfufu.testdimension.network.ModNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DimensionConfigScreen extends Screen {
    private static final int SCREEN_WIDTH = 380;
    private static final int SCREEN_HEIGHT = 240;

    private DimensionTypeConfig typeConfig;
    private DimensionGeneratorConfig dimConfig;

    private EditBox coordinateScale;
    private EditBox ambientLight;
    private EditBox effects;
    private EditBox infiniburn;
    private EditBox minY;
    private EditBox heightVal;
    private EditBox logicalHeight;
    private EditBox monsterSpawnLight;
    private EditBox monsterSpawnBlockLimit;
    private EditBox generatorBiome;
    private EditBox generatorLayers;
    private EditBox datapackInput;

    private CycleButton<Boolean> ultrawarm;
    private CycleButton<Boolean> natural;
    private CycleButton<Boolean> bedWorks;
    private CycleButton<Boolean> respawnAnchor;
    private CycleButton<Boolean> hasSkylight;
    private CycleButton<Boolean> hasCeiling;
    private CycleButton<Boolean> hasRaids;
    private CycleButton<Boolean> piglinSafe;
    private CycleButton<Boolean> generatorLakes;
    private CycleButton<Boolean> generatorFeatures;

    private int tabIndex;
    private Button btnType;
    private Button btnGenerator;
    private Button btnJson;

    public DimensionConfigScreen() {
        super(Component.literal("Dimension Configuration"));
        this.typeConfig = DimDataModifier.getCurrentTypeConfig();
        this.dimConfig = DimDataModifier.getCurrentDimConfig();
        this.tabIndex = 0;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        int cx = (this.width - SCREEN_WIDTH) / 2;
        int cy = (this.height - SCREEN_HEIGHT) / 2;
        int col2 = cx + 200;
        int row = cy + 60;
        int rh = 20;

        coordinateScale = newEditBox(cx + 120, row, 60, "1.0");
        ambientLight = newEditBox(col2 + 90, row, 50, "0.0");
        row += rh;

        ultrawarm = newBoolBtn(cx + 80, row, false);
        natural = newBoolBtn(col2 + 20, row, true);
        row += rh;
        bedWorks = newBoolBtn(cx + 80, row, true);
        respawnAnchor = newBoolBtn(col2 + 20, row, false);
        row += rh;
        hasSkylight = newBoolBtn(cx + 80, row, true);
        hasCeiling = newBoolBtn(col2 + 20, row, false);
        row += rh;
        hasRaids = newBoolBtn(cx + 80, row, true);
        piglinSafe = newBoolBtn(col2 + 20, row, false);
        row += rh + 2;

        effects = newEditBox(cx + 80, row, 120, "minecraft:overworld");
        row += rh;
        infiniburn = newEditBox(cx + 80, row, 200, "#minecraft:infiniburn_overworld");
        row += rh;
        minY = newEditBox(cx + 65, row, 45, "-64");
        heightVal = newEditBox(cx + 160, row, 45, "384");
        logicalHeight = newEditBox(cx + 275, row, 45, "384");
        row += rh;
        monsterSpawnLight = newEditBox(cx + 115, row, 50, "0-7");
        monsterSpawnBlockLimit = newEditBox(cx + 255, row, 40, "0");

        generatorBiome = newEditBox(cx + 80, cy + 56, 130, "minecraft:the_void");
        generatorLayers = newEditBox(cx + 80, cy + 78, 150, "64x minecraft:iron_block");
        generatorLakes = newBoolBtn(cx + 80, cy + 100, false);
        generatorFeatures = newBoolBtn(col2 + 10, cy + 100, false);

        datapackInput = newEditBox(cx + 160, cy + 227, 110, "minecraft:overworld");

        btnType = Button.builder(Component.literal("Type"), btn -> { tabIndex = 0; toggleTabVisibility(); })
                .bounds(cx + 10, cy + 28, 60, 20).build();
        addRenderableWidget(btnType);
        btnGenerator = Button.builder(Component.literal("Generator"), btn -> { tabIndex = 1; toggleTabVisibility(); })
                .bounds(cx + 72, cy + 28, 60, 20).build();
        addRenderableWidget(btnGenerator);
        btnJson = Button.builder(Component.literal("JSON"), btn -> { tabIndex = 2; toggleTabVisibility(); })
                .bounds(cx + 134, cy + 28, 60, 20).build();
        addRenderableWidget(btnJson);

        addRenderableWidget(Button.builder(Component.literal("Load"), btn -> loadFromDatapack())
                .bounds(cx + 275, cy + 227, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Apply & Reload"), btn -> applyAndSave())
                .bounds(cx + 10, cy + 215, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(cx + SCREEN_WIDTH - 50, cy + 215, 40, 20).build());

        toggleTabVisibility();
        refreshFromConfig();
    }

    private void toggleTabVisibility() {
        boolean t0 = tabIndex == 0;
        boolean t1 = tabIndex == 1;
        boolean t2 = tabIndex == 2;

        setVis(coordinateScale, t0);
        setVis(ambientLight, t0);
        setVis(effects, t0);
        setVis(infiniburn, t0);
        setVis(minY, t0);
        setVis(heightVal, t0);
        setVis(logicalHeight, t0);
        setVis(monsterSpawnLight, t0);
        setVis(monsterSpawnBlockLimit, t0);
        setVis(ultrawarm, t0);
        setVis(natural, t0);
        setVis(bedWorks, t0);
        setVis(respawnAnchor, t0);
        setVis(hasSkylight, t0);
        setVis(hasCeiling, t0);
        setVis(hasRaids, t0);
        setVis(piglinSafe, t0);

        setVis(generatorBiome, t1);
        setVis(generatorLayers, t1);
        setVis(generatorLakes, t1);
        setVis(generatorFeatures, t1);

        datapackInput.setVisible(t2);
    }

    private static void setVis(EditBox box, boolean vis) {
        box.setVisible(vis);
    }

    private static void setVis(CycleButton<?> btn, boolean vis) {
        btn.visible = vis;
    }

    private EditBox newEditBox(int x, int y, int w, String def) {
        EditBox box = new EditBox(font, x, y, w, 16, Component.literal(def));
        box.setValue(def);
        addRenderableWidget(box);
        return box;
    }

    private CycleButton<Boolean> newBoolBtn(int x, int y, boolean def) {
        CycleButton<Boolean> btn = CycleButton.booleanBuilder(Component.literal("ON"), Component.literal("OFF"))
                .displayOnlyValue().withInitialValue(def)
                .create(x, y, 44, 16, Component.literal(""), (cb, val) -> {});
        addRenderableWidget(btn);
        return btn;
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int cx = (this.width - SCREEN_WIDTH) / 2;
        int cy = (this.height - SCREEN_HEIGHT) / 2;
        graphics.drawString(font, "Dimension Configuration", cx + 10, cy + 8, 0xFFFFFF);

        if (tabIndex == 0) {
            renderTypeTab(graphics, cx, cy);
        } else if (tabIndex == 1) {
            renderGeneratorTab(graphics, cx, cy);
        } else {
            renderJsonTab(graphics, cx, cy);
        }
    }

    private void renderTypeTab(GuiGraphics g, int cx, int cy) {
        int row = cy + 62; int rh = 20;
        g.drawString(font, "coordinate_scale:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "ambient_light:", cx + 190, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "ultrawarm:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "natural:", cx + 140, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "bed_works:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "respawn_anchor:", cx + 140, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "has_skylight:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "has_ceiling:", cx + 140, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "has_raids:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "piglin_safe:", cx + 140, row, 0xAAAAAA);
        row += rh + 2;
        g.drawString(font, "effects:", cx + 10, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "infiniburn:", cx + 10, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "min_y:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "height:", cx + 120, row, 0xAAAAAA);
        g.drawString(font, "logical_h:", cx + 215, row, 0xAAAAAA);
        row += rh;
        g.drawString(font, "monster_light:", cx + 10, row, 0xAAAAAA);
        g.drawString(font, "block_limit:", cx + 190, row, 0xAAAAAA);
    }

    private void renderGeneratorTab(GuiGraphics g, int cx, int cy) {
        g.drawString(font, "Biome:", cx + 10, cy + 62, 0xAAAAAA);
        g.drawString(font, "Layers:", cx + 10, cy + 84, 0xAAAAAA);
        g.drawString(font, "Lakes:", cx + 10, cy + 106, 0xAAAAAA);
        g.drawString(font, "Features:", cx + 140, cy + 106, 0xAAAAAA);
    }

    private void renderJsonTab(GuiGraphics g, int cx, int cy) {
        String typeJson = DimDataModifier.buildTypeJson();
        String dimJson = DimDataModifier.buildDimensionJson();
        g.drawString(font, "Dimension Type JSON:", cx + 10, cy + 56, 0xAAAAAA);
        for (String line : limitLines(typeJson, 6)) {
            g.drawString(font, line, cx + 14, cy + 72 + limitLines(typeJson, 6).indexOf(line) * 10, 0xCCCCCC);
        }
        g.drawString(font, "Dimension JSON:", cx + 10, cy + 140, 0xAAAAAA);
        for (String line : limitLines(dimJson, 4)) {
            g.drawString(font, line, cx + 14, cy + 156 + limitLines(dimJson, 4).indexOf(line) * 10, 0xCCCCCC);
        }
    }

    private List<String> limitLines(String s, int max) {
        String[] arr = s.split("\n");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(arr.length, max); i++) {
            result.add(arr[i]);
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        float oldTab = this.tabIndex;
        super.resize(minecraft, width, height);
        this.tabIndex = (int) oldTab;
    }

    public void onDatapackResponse(String typeConfigJson, String dimConfigJson, String dimensionId, String error) {
        if (error != null && !error.isEmpty()) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("Error: " + error), false);
            }
            return;
        }
        try {
            typeConfig = DimensionTypeConfig.fromJson(typeConfigJson);
            if (dimConfigJson != null && !dimConfigJson.isEmpty()) {
                dimConfig = DimensionGeneratorConfig.fromJson(dimConfigJson);
            }
            refreshFromConfig();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("Loaded dimension data from: " + dimensionId), false);
            }
        } catch (Exception e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("Failed to parse: " + e.getMessage()), false);
            }
        }
    }

    private void refreshFromConfig() {
        coordinateScale.setValue(String.valueOf(typeConfig.coordinate_scale));
        ambientLight.setValue(String.valueOf(typeConfig.ambient_light));
        effects.setValue(typeConfig.effects != null ? typeConfig.effects : "minecraft:overworld");
        infiniburn.setValue(typeConfig.infiniburn != null ? typeConfig.infiniburn : "#minecraft:infiniburn_overworld");
        minY.setValue(String.valueOf(typeConfig.min_y));
        heightVal.setValue(String.valueOf(typeConfig.height));
        logicalHeight.setValue(String.valueOf(typeConfig.logical_height));
        monsterSpawnLight.setValue(typeConfig.monster_spawn_light_level != null ? typeConfig.monster_spawn_light_level : "0-7");
        monsterSpawnBlockLimit.setValue(String.valueOf(typeConfig.monster_spawn_block_light_limit));
        ultrawarm.setValue(typeConfig.ultrawarm);
        natural.setValue(typeConfig.natural);
        bedWorks.setValue(typeConfig.bed_works);
        respawnAnchor.setValue(typeConfig.respawn_anchor_works);
        hasSkylight.setValue(typeConfig.has_skylight);
        hasCeiling.setValue(typeConfig.has_ceiling);
        hasRaids.setValue(typeConfig.has_raids);
        piglinSafe.setValue(typeConfig.piglin_safe);

        if (dimConfig.flatSettings != null) {
            generatorBiome.setValue(dimConfig.flatSettings.biome != null ? dimConfig.flatSettings.biome : "minecraft:the_void");
            StringBuilder sb = new StringBuilder();
            if (dimConfig.flatSettings.layers != null) {
                for (var layer : dimConfig.flatSettings.layers) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(layer.height).append("x ").append(layer.block);
                }
            }
            generatorLayers.setValue(!sb.isEmpty() ? sb.toString() : "64x minecraft:iron_block");
            generatorLakes.setValue(dimConfig.flatSettings.lakes);
            generatorFeatures.setValue(dimConfig.flatSettings.features);
        }
    }

    private void applyAndSave() {
        try {
            typeConfig.coordinate_scale = parseDouble(coordinateScale.getValue(), 1.0);
            typeConfig.ambient_light = parseDouble(ambientLight.getValue(), 0.0);
            typeConfig.effects = effects.getValue();
            typeConfig.infiniburn = infiniburn.getValue();
            typeConfig.min_y = parseInt(minY.getValue(), -64);
            typeConfig.height = parseInt(heightVal.getValue(), 384);
            typeConfig.logical_height = parseInt(logicalHeight.getValue(), 384);
            typeConfig.monster_spawn_light_level = monsterSpawnLight.getValue();
            typeConfig.monster_spawn_block_light_limit = parseInt(monsterSpawnBlockLimit.getValue(), 0);
            typeConfig.ultrawarm = ultrawarm.getValue();
            typeConfig.natural = natural.getValue();
            typeConfig.bed_works = bedWorks.getValue();
            typeConfig.respawn_anchor_works = respawnAnchor.getValue();
            typeConfig.has_skylight = hasSkylight.getValue();
            typeConfig.has_ceiling = hasCeiling.getValue();
            typeConfig.has_raids = hasRaids.getValue();
            typeConfig.piglin_safe = piglinSafe.getValue();

            if (dimConfig.flatSettings == null) {
                dimConfig.flatSettings = new DimensionGeneratorConfig.FlatSettings();
            }
            dimConfig.flatSettings.biome = generatorBiome.getValue();
            dimConfig.flatSettings.lakes = generatorLakes.getValue();
            dimConfig.flatSettings.features = generatorFeatures.getValue();
            parseLayers(generatorLayers.getValue(), dimConfig.flatSettings);

            DimDataModifier.updateTypeConfig(typeConfig);
            DimDataModifier.updateDimConfig(dimConfig);

            PacketDistributor.sendToServer(new ModNetwork.ApplyDimConfigPayload(
                    typeConfig.toJson(), dimConfig.toPrettyJson()));

            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("Config sent. Server will hot-reload."), false);
            }
        } catch (Exception e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("Error: " + e.getMessage()), false);
            }
        }
    }

    private void loadFromDatapack() {
        String input = datapackInput.getValue().trim();
        if (input.isEmpty()) input = "minecraft:overworld";
        ResourceLocation loc = ResourceLocation.tryParse(input);
        if (loc == null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("Invalid resource location."), false);
            }
            return;
        }
        PacketDistributor.sendToServer(new ModNetwork.RequestDatapackDimPayload(loc));
    }

    private void parseLayers(String layersStr, DimensionGeneratorConfig.FlatSettings settings) {
        settings.layers.clear();
        if (layersStr == null || layersStr.isBlank()) {
            settings.layers.add(new DimensionGeneratorConfig.Layer(64, "minecraft:iron_block"));
            return;
        }
        for (String part : layersStr.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            String[] seg = part.split("x\\s*", 2);
            int height = 1;
            String block = part;
            if (seg.length == 2) {
                try { height = Integer.parseInt(seg[0].trim()); } catch (NumberFormatException ignored) {}
                block = seg[1].trim();
            }
            settings.layers.add(new DimensionGeneratorConfig.Layer(height, block));
        }
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
