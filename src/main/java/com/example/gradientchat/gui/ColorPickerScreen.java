package com.example.gradientchat.gui;

import java.awt.Color;
import java.util.function.Consumer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final Consumer<Integer> colorCallback;
    private int selectedColor;
    
    private int hue = 0;
    private int saturation = 100;
    private int brightness = 100;
    
    private TextFieldWidget hexInput;
    
    public ColorPickerScreen(Screen parent, int initialColor, Consumer<Integer> colorCallback) {
        super(Text.literal("Color Picker"));
        this.parent = parent;
        this.selectedColor = initialColor;
        this.colorCallback = colorCallback;
        
        // Convert RGB to HSB
        float[] hsb = Color.RGBtoHSB(
                (initialColor >> 16) & 0xFF,
                (initialColor >> 8) & 0xFF,
                initialColor & 0xFF,
                null
        );
        
        this.hue = (int) (hsb[0] * 360);
        this.saturation = (int) (hsb[1] * 100);
        this.brightness = (int) (hsb[2] * 100);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Add hex input field
        hexInput = new TextFieldWidget(
                textRenderer,
                centerX - 40,
                centerY + 70,
                80,
                20,
                Text.literal("Hex")
        );
        hexInput.setMaxLength(7); // #RRGGBB
        hexInput.setText(String.format("#%06X", selectedColor));
        hexInput.setChangedListener(this::onHexChanged);
        addDrawableChild(hexInput);
        
        // Add done button
        ButtonWidget doneButton = ButtonWidget.builder(
                Text.literal("Done"),
                button -> {
                    colorCallback.accept(selectedColor);
                    MinecraftClient.getInstance().setScreen(parent);
                }
        )
                .dimensions(centerX - 50, centerY + 100, 100, 20)
                .build();
        addDrawableChild(doneButton);
    }
    
    private void onHexChanged(String text) {
        if (text.startsWith("#") && text.length() == 7) {
            try {
                int color = Integer.parseInt(text.substring(1), 16);
                selectedColor = color;
                
                // Update HSB values
                float[] hsb = Color.RGBtoHSB(
                        (color >> 16) & 0xFF,
                        (color >> 8) & 0xFF,
                        color & 0xFF,
                        null
                );
                
                hue = (int) (hsb[0] * 360);
                saturation = (int) (hsb[1] * 100);
                brightness = (int) (hsb[2] * 100);
            } catch (NumberFormatException e) {
                // Invalid hex, ignore
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fill with solid background color
        context.fill(0, 0, width, height, 0xDD1F1F1F);
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Draw color picker panel
        int panelWidth = 200;
        int panelHeight = 220;
        context.fill(centerX - panelWidth/2, centerY - panelHeight/2, 
                    centerX + panelWidth/2, centerY + panelHeight/2, 0xFF333333);
        context.drawBorder(centerX - panelWidth/2, centerY - panelHeight/2, 
                          panelWidth, panelHeight, 0xFFFFFFFF);
        
        // Draw title
        context.drawCenteredTextWithShadow(textRenderer, "Color Picker", centerX, centerY - 100, 0xFFFFFF);
        
        // Draw hue slider
        int hueSliderWidth = 180;
        int hueSliderHeight = 15;
        int hueSliderX = centerX - hueSliderWidth/2;
        int hueSliderY = centerY - 70;
        
        // Draw hue slider background
        context.fill(hueSliderX, hueSliderY, hueSliderX + hueSliderWidth, hueSliderY + hueSliderHeight, 0xFF000000);
        
        // Draw hue gradient
        for (int i = 0; i < hueSliderWidth; i++) {
            float hueValue = (float) i / hueSliderWidth;
            int hueColor = Color.HSBtoRGB(hueValue, 1.0f, 1.0f);
            context.fill(
                    hueSliderX + i,
                    hueSliderY,
                    hueSliderX + i + 1,
                    hueSliderY + hueSliderHeight,
                    0xFF000000 | hueColor
            );
        }
        
        // Draw hue selector
        int hueX = hueSliderX + (int) (hueSliderWidth * (hue / 360.0f));
        context.drawBorder(
                hueX - 2,
                hueSliderY - 2,
                5,
                hueSliderHeight + 4,
                0xFFFFFFFF
        );
        
        // Draw saturation/brightness grid
        int gridSize = 100;
        int gridX = centerX - gridSize/2;
        int gridY = centerY - 40;
        
        // Draw grid background
        context.fill(gridX, gridY, gridX + gridSize, gridY + gridSize, 0xFF000000);
        
        // Draw saturation/brightness gradient
        for (int s = 0; s < gridSize; s++) {
            for (int b = 0; b < gridSize; b++) {
                float satValue = (float) s / gridSize;
                float briValue = (float) (gridSize - b) / gridSize;
                int color = Color.HSBtoRGB(hue / 360.0f, satValue, briValue);
                context.fill(
                        gridX + s,
                        gridY + b,
                        gridX + s + 1,
                        gridY + b + 1,
                        0xFF000000 | color
                );
            }
        }
        
        // Draw saturation/brightness selector
        int satX = gridX + (int) (saturation / 100.0f * gridSize);
        int briY = gridY + (int) ((100 - brightness) / 100.0f * gridSize);
        
        // Draw crosshair
        context.fill(satX - 5, briY, satX + 5, briY + 1, 0xFFFFFFFF);
        context.fill(satX, briY - 5, satX + 1, briY + 5, 0xFFFFFFFF);
        context.drawBorder(satX - 3, briY - 3, 7, 7, 0xFFFFFFFF);
        
        // Draw selected color preview
        int previewSize = 30;
        int previewX = centerX - 60;
        int previewY = centerY + 65;
        
        // Draw selected color
        context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | selectedColor);
        context.drawBorder(previewX, previewY, previewSize, previewSize, 0xFFFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = width / 2;
            int centerY = height / 2;
            
            // Check if clicked on hue slider
            int hueSliderWidth = 180;
            int hueSliderHeight = 15;
            int hueSliderX = centerX - hueSliderWidth/2;
            int hueSliderY = centerY - 70;
            
            if (mouseY >= hueSliderY && mouseY <= hueSliderY + hueSliderHeight &&
                    mouseX >= hueSliderX && mouseX <= hueSliderX + hueSliderWidth) {
                hue = (int) ((mouseX - hueSliderX) / hueSliderWidth * 360);
                updateSelectedColor();
                return true;
            }
            
            // Check if clicked on saturation/brightness grid
            int gridSize = 100;
            int gridX = centerX - gridSize/2;
            int gridY = centerY - 40;
            
            if (mouseX >= gridX && mouseX <= gridX + gridSize &&
                    mouseY >= gridY && mouseY <= gridY + gridSize) {
                saturation = (int) ((mouseX - gridX) / gridSize * 100);
                brightness = (int) ((gridSize - (mouseY - gridY)) / gridSize * 100);
                updateSelectedColor();
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            int centerX = width / 2;
            int centerY = height / 2;
            
            // Check if dragging on hue slider
            int hueSliderWidth = 180;
            int hueSliderHeight = 15;
            int hueSliderX = centerX - hueSliderWidth/2;
            int hueSliderY = centerY - 70;
            
            if (mouseY >= hueSliderY - 5 && mouseY <= hueSliderY + hueSliderHeight + 5) {
                int newHue = (int) ((mouseX - hueSliderX) / hueSliderWidth * 360);
                hue = Math.max(0, Math.min(359, newHue));
                updateSelectedColor();
                return true;
            }
            
            // Check if dragging on saturation/brightness grid
            int gridSize = 100;
            int gridX = centerX - gridSize/2;
            int gridY = centerY - 40;
            
            // Allow dragging slightly outside the grid
            int newSat = (int) ((mouseX - gridX) / gridSize * 100);
            int newBri = (int) ((gridSize - (mouseY - gridY)) / gridSize * 100);
            
            saturation = Math.max(0, Math.min(100, newSat));
            brightness = Math.max(0, Math.min(100, newBri));
            updateSelectedColor();
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private void updateSelectedColor() {
        selectedColor = Color.HSBtoRGB(hue / 360.0f, saturation / 100.0f, brightness / 100.0f) & 0xFFFFFF;
        hexInput.setText(String.format("#%06X", selectedColor));
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // Escape key
            colorCallback.accept(selectedColor);
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    // Override to prevent any blur
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Explicitly fill with solid color to prevent blur
        context.fill(0, 0, this.width, this.height, 0xFF1F1F1F);
    }
}
