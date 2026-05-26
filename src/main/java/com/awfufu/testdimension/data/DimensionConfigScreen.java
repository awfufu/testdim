package com.awfufu.testdimension.data;

import com.awfufu.testdimension.network.ModNetwork;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

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
    private boolean showDatapackInput;

    public DimensionConfigScreen() {
        super(Component.literal("Dimension Configuration"));
        this.typeConfig = DimDataModifier.getCurrentTypeConfig();
        this.dimConfig = DimDataModifier.getCurrentDimConfig();
        this.tabIndex = 0;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = (this.width - SCREEN_WIDTH) / 2;
        int centerY = (this.height - SCREEN_HEIGHT) / 2;



        int col2 = centerX + 200;
        int row = centerY + 56;
        int rh = 20;

        coordinateScale = createEditBox(centerX + 120, row, 60, "1.0",0);
        ambientLight = createEditBox(col2 + 90, row, 50, "0.0",0);
        row += rh;

        ultrawarm = createBoolBtn(centerX + 70, row, false,0);
        natural = createBoolBtn(col2 + 10, row, true,0);
        bedWorks = createBoolBtn(centerX + 70, row += rh, true,0);
        respawnAnchor = createBoolBtn(col2 + 10, row, false,0);
        hasSkylight = createBoolBtn(centerX + 70, row += rh, true,0);
        hasCeiling = createBoolBtn(col2 + 10, row, false,0);
        hasRaids = createBoolBtn(centerX + 70, row += rh, true,0);
        piglinSafe = createBoolBtn(col2 + 10, row, false,0);
        row += rh + 2;

        effects = createEditBox(centerX + 80, row, 120, "minecraft:overworld",0);
        row += rh;
        infiniburn = createEditBox(centerX + 80, row, 200, "#minecraft:infiniburn_overworld",0);
        row += rh;
        minY = createEditBox(centerX + 65, row, 45, "-64",0);
        heightVal = createEditBox(centerX + 160, row, 45, "384",0);
        logicalHeight = createEditBox(centerX + 275, row, 45, "384",0);
        row += rh;
        monsterSpawnLight = createEditBox(centerX + 115, row, 50, "0-7",0);
        monsterSpawnBlockLimit = createEditBox(centerX + 255, row, 40, "0",0);
        row += rh + 4;

        generatorBiome = createEditBox(centerX + 80, centerY + 56, 130, "minecraft:the_void",1);
        generatorLayers = createEditBox(centerX + 80, centerY + 78, 150, "64x minecraft:iron_block",1);
        generatorLakes = createBoolBtn(centerX + 70, centerY + 100, false,1);
        generatorFeatures = createBoolBtn(col2 + 10, centerY + 100, false,1);

        datapackInput = createEditBox(centerX + 160, centerY + 227, 110, "minecraft:overworld",2);



        refreshFromConfig();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int centerX = (this.width - SCREEN_WIDTH) / 2;
        int centerY = (this.height - SCREEN_HEIGHT) / 2;
        clearWidgets();
        graphics.drawString(font, "Dimension Configuration", centerX + 10, centerY + 8, 0xFFFFFF);
        addRenderableWidget(Button.builder(Component.literal("Type"), btn -> tabIndex = 0)
                .bounds(centerX + 10, centerY + 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Generator"), btn -> tabIndex = 1)
                .bounds(centerX + 72, centerY + 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("JSON"), btn -> tabIndex = 2)
                .bounds(centerX + 134, centerY + 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Load"), btn -> loadFromDatapack())
                .bounds(centerX + 275, centerY + 227, 40, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Apply & Save"), btn -> applyAndSave())
                .bounds(centerX + 10, centerY + 215, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(centerX + SCREEN_WIDTH - 50, centerY + 215, 40, 20).build());
        if (tabIndex == 0) {
            renderTypeTab(graphics, centerX, centerY, partialTick);
        } else if (tabIndex == 1) {
            renderGeneratorTab(graphics, centerX, centerY, partialTick);
        } else {
            renderJsonTab(graphics, centerX, centerY, partialTick);
        }
    }

    private void renderTypeTab(GuiGraphics graphics, int cx, int cy,float pt) {

        for (Renderable renderable : this.rendTab1) {
            renderable.render(graphics, cx, cy, pt);
            this.addWidget((GuiEventListener & NarratableEntry)renderable);
        }

        int row = cy + 58;
        int rh = 20;
        graphics.drawString(font, "coordinate_scale:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "ambient_light:", cx + 190, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "ultrawarm:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "natural:", cx + 140, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "bed_works:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "respawn_anchor:", cx + 140, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "has_skylight:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "has_ceiling:", cx + 140, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "has_raids:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "piglin_safe:", cx + 140, row + 6, 0xAAAAAA);
        row += rh + 2;
        graphics.drawString(font, "effects:", cx + 10, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "infiniburn:", cx + 10, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "min_y:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "height:", cx + 120, row + 6, 0xAAAAAA);
        graphics.drawString(font, "logical_h:", cx + 215, row + 6, 0xAAAAAA);
        row += rh;
        graphics.drawString(font, "monster_light:", cx + 10, row + 6, 0xAAAAAA);
        graphics.drawString(font, "block_limit:", cx + 190, row + 6, 0xAAAAAA);
    }

    private void renderGeneratorTab(GuiGraphics graphics, int cx, int cy,float pt) {

        for (Renderable renderable : this.rendTab1) {
            renderable.render(graphics, cx, cy, pt);
            this.addWidget((GuiEventListener & NarratableEntry)renderable);
        }
        graphics.drawString(font, "Biome:", cx + 10, cy + 62, 0xAAAAAA);
        graphics.drawString(font, "Layers:", cx + 10, cy + 84, 0xAAAAAA);
        graphics.drawString(font, "Lakes:", cx + 10, cy + 106, 0xAAAAAA);
        graphics.drawString(font, "Features:", cx + 140, cy + 106, 0xAAAAAA);
    }

    private void renderJsonTab(GuiGraphics graphics, int cx, int cy,float pt) {

        for (Renderable renderable : this.rendTab1) {
            renderable.render(graphics, cx, cy, pt);
            this.addWidget((GuiEventListener & NarratableEntry)renderable);
        }
        String typeJson = DimDataModifier.buildTypeJson();
        String dimJson = DimDataModifier.buildDimensionJson();
        graphics.drawString(font, "Dimension Type JSON:", cx + 10, cy + 56, 0xAAAAAA);
        String[] typeLines = typeJson.split("\n");
        for (int i = 0; i < Math.min(typeLines.length, 6); i++) {
            graphics.drawString(font, typeLines[i], cx + 14, cy + 72 + i * 10, 0xCCCCCC);
        }
        graphics.drawString(font, "Dimension JSON:", cx + 10, cy + 140, 0xAAAAAA);
        String[] dimLines = dimJson.split("\n");
        for (int i = 0; i < Math.min(dimLines.length, 4); i++) {
            graphics.drawString(font, dimLines[i], cx + 14, cy + 156 + i * 10, 0xCCCCCC);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
    public final List<Renderable> rendTab1 = Lists.newArrayList();
    public final List<Renderable> rendTab2 = Lists.newArrayList();
    public final List<Renderable> rendTab3 = Lists.newArrayList();

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T renderable, int tabIndex) {
        switch (tabIndex) {
            case 0:
                rendTab1.add(renderable);
                break;
            case 1:
                rendTab2.add(renderable);
                break;
            case 2:
                rendTab3.add(renderable);
                break;
            default:
                break;
        }
        return renderable;
    }


    private EditBox createEditBox(int x, int y, int w, String defaultValue,int tabIndex) {
        EditBox box = new EditBox(font, x, y, w, 16, Component.literal(defaultValue));
        box.setValue(defaultValue);
        addRenderableWidget(box,tabIndex);
        return box;
    }

    private CycleButton<Boolean> createBoolBtn(int x, int y, boolean defaultValue,int tabIndex) {
        CycleButton<Boolean> btn = CycleButton.booleanBuilder(Component.literal("ON"), Component.literal("OFF"))
                .displayOnlyValue()
                .withInitialValue(defaultValue)
                .create(x, y, 44, 16, Component.literal(""), (cb, val) -> {
                });
        addRenderableWidget(btn,tabIndex);
        return btn;
    }

    private void refreshFromConfig() {
        coordinateScale.setValue(String.valueOf(typeConfig.coordinate_scale));
        ambientLight.setValue(String.valueOf(typeConfig.ambient_light));
        effects.setValue(typeConfig.effects != null ? typeConfig.effects : "minecraft:overworld");
        infiniburn.setValue(
                typeConfig.infiniburn != null ? typeConfig.infiniburn : "#minecraft:infiniburn_overworld");
        minY.setValue(String.valueOf(typeConfig.min_y));
        heightVal.setValue(String.valueOf(typeConfig.height));
        logicalHeight.setValue(String.valueOf(typeConfig.logical_height));
        monsterSpawnLight
                .setValue(typeConfig.monster_spawn_light_level != null ? typeConfig.monster_spawn_light_level : "0-7");
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
            generatorBiome.setValue(
                    dimConfig.flatSettings.biome != null ? dimConfig.flatSettings.biome : "minecraft:the_void");
            StringBuilder layersStr = new StringBuilder();
            if (dimConfig.flatSettings.layers != null) {
                for (var layer : dimConfig.flatSettings.layers) {
                    if (!layersStr.isEmpty())
                        layersStr.append(", ");
                    layersStr.append(layer.height).append("x ").append(layer.block);
                }
            }
            generatorLayers.setValue(!layersStr.isEmpty() ? layersStr.toString() : "64x minecraft:iron_block");
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

            PacketDistributor.sendToServer(new ModNetwork.ApplyDimConfigPayload(
                    typeConfig.toJson(), dimConfig.toPrettyJson()));

            DimDataModifier.updateTypeConfig(typeConfig);
            DimDataModifier.updateDimConfig(dimConfig);

            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("Config sent to server."), false);
            }
        } catch (Exception e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("Error: " + e.getMessage()), false);
            }
        }
    }

    private void loadFromDatapack() {
        String input = datapackInput.getValue().trim();
        if (input.isEmpty()) {
            input = "minecraft:overworld";
        }
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
        String[] parts = layersStr.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty())
                continue;
            String[] segments = part.split("x\\s*", 2);
            int height = 1;
            String block = part;
            if (segments.length == 2) {
                try {
                    height = Integer.parseInt(segments[0].trim());
                } catch (NumberFormatException ignored) {
                }
                block = segments[1].trim();
            }
            settings.layers.add(new DimensionGeneratorConfig.Layer(height, block));
        }
    }

    private static double parseDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
