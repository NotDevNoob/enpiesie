package com.example.gradientchat.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.example.gradientchat.GradientManager;
import com.example.gradientchat.GradientManager.GradientPreset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class GradientScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLOR_BUTTON_SIZE = 20;
    private static final int SPACING = 8;
    
    private final GradientManager gradientManager;
    private final List<ColorButton> colorButtons = new ArrayList<>();
    private final List<ButtonWidget> presetButtons = new ArrayList<>();
    
    private TextFieldWidget previewField;
    private String previewText = "Preview your gradient text here!";
    private int selectedPreset;
    private int selectedColorIndex = -1;
    
    public GradientScreen(GradientManager gradientManager) {
        super(Text.literal("Gradient Chat Settings"));
        this.gradientManager = gradientManager;
        this.selectedPreset = gradientManager.getPresets().indexOf(gradientManager.getCurrentPreset());
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int startY = 40;
        int currentY = startY;
        
        // Title
        currentY += 10;
        
        // Add preset buttons in a vertical list
        List<GradientPreset> presets = gradientManager.getPresets();
        for (int i = 0; i < presets.size(); i++) {
            final int presetIndex = i;
            ButtonWidget presetButton = ButtonWidget.builder(
                    Text.literal(presets.get(i).getName()),
                    button -> selectPreset(presetIndex)
            )
                    .dimensions(centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            
            addDrawableChild(presetButton);
            presetButtons.add(presetButton);
            currentY += BUTTON_HEIGHT + 4;
        }
        
        // Highlight the selected preset
        updatePresetButtonStates();
        
        currentY += 10;
        
        // Add color buttons for the selected preset in a grid
        updateColorButtons(centerX, currentY);
        currentY += ((gradientManager.getPresets().get(selectedPreset).getColors().length + 4) / 5) * 
                    (COLOR_BUTTON_SIZE + SPACING) + 20;
        
        // Add preview text field
        previewField = new TextFieldWidget(
                textRenderer,
                centerX - 100,
                currentY,
                200,
                20,
                Text.literal("Preview Text")
        );
        previewField.setMaxLength(100);
        previewField.setText(previewText);
        addDrawableChild(previewField);
        
        // Add preview button
        ButtonWidget previewButton = ButtonWidget.builder(
                Text.literal("Preview"),
                button -> {
                    String text = previewField.getText();
                    if (text != null && !text.isEmpty()) {
                        previewText = text;
                    }
                }
        )
                .dimensions(centerX - 50, currentY + 25, 100, 20)
                .build();
        addDrawableChild(previewButton);
    }
    
    private void selectPreset(int index) {
        selectedPreset = index;
        updatePresetButtonStates();
        
        // Update color buttons
        int centerX = width / 2;
        int startY = 40;
        int currentY = startY + 10 + presetButtons.size() * (BUTTON_HEIGHT + 4) + 10;
        
        updateColorButtons(centerX, currentY);
    }
    
    private void updatePresetButtonStates() {
        for (int i = 0; i < presetButtons.size(); i++) {
            ButtonWidget button = presetButtons.get(i);
            if (i == selectedPreset) {
                // Make selected button stand out
                button.active = false;
                button.setMessage(Text.literal("▶ " + gradientManager.getPresets().get(i).getName()));
            } else {
                button.active = true;
                button.setMessage(Text.literal(gradientManager.getPresets().get(i).getName()));
            }
        }
    }
    
    private void updateColorButtons(int centerX, int startY) {
        // Remove existing color buttons
        colorButtons.forEach(this::remove);
        colorButtons.clear();
        
        // Add new color buttons for the selected preset
        GradientPreset preset = gradientManager.getPresets().get(selectedPreset);
        int[] colors = preset.getColors();
        
        int buttonsPerRow = 5;
        int rowWidth = buttonsPerRow * (COLOR_BUTTON_SIZE + SPACING) - SPACING;
        int startX = centerX - rowWidth / 2;
        
        for (int i = 0; i < colors.length; i++) {
            final int colorIndex = i;
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            ColorButton colorButton = new ColorButton(
                    startX + col * (COLOR_BUTTON_SIZE + SPACING),
                    startY + row * (COLOR_BUTTON_SIZE + SPACING),
                    COLOR_BUTTON_SIZE,
                    COLOR_BUTTON_SIZE,
                    Text.literal(""),
                    button -> selectColor(colorIndex),
                    colors[i]
            );
            
            addDrawableChild(colorButton);
            colorButtons.add(colorButton);
        }
    }
    
    private void selectColor(int colorIndex) {
        selectedColorIndex = colorIndex;
        
        // Open a simple color picker dialog
        MinecraftClient.getInstance().setScreen(new ColorPickerScreen(
                this,
                gradientManager.getPresets().get(selectedPreset).getColors()[colorIndex],
                color -> {
                    gradientManager.getPresets().get(selectedPreset).setColor(colorIndex, color);
                    colorButtons.get(colorIndex).setColor(color);
                }
        ));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fill with solid background color
        context.fill(0, 0, width, height, 0xFF1F1F1F);
        
        // Draw title
        String titleText = "Gradient Chat Settings";
        int titleWidth = textRenderer.getWidth(titleText);
        context.drawCenteredTextWithShadow(textRenderer, titleText, width / 2, 15, 0xFFFFFF);
        
        // Draw preview section title
        int previewY = 40 + 10 + presetButtons.size() * (BUTTON_HEIGHT + 4) + 10 + 
                      ((gradientManager.getPresets().get(selectedPreset).getColors().length + 4) / 5) * 
                      (COLOR_BUTTON_SIZE + SPACING);
        
        context.drawCenteredTextWithShadow(textRenderer, "Preview", width / 2, previewY - 10, 0xFFFFFF);
        
        // Draw preview with gradient
        int gradientY = previewY + 50;
        
        // Get the colors from the current preset
        GradientPreset preset = gradientManager.getPresets().get(selectedPreset);
        int[] colors = preset.getColors();
        
        // Calculate how many characters per color segment
        int messageLength = previewText.length();
        double charsPerSegment = (double) messageLength / (colors.length - 1);
        
        // Calculate total width to center the text
        int totalWidth = 0;
        for (int i = 0; i < previewText.length(); i++) {
            totalWidth += textRenderer.getWidth(String.valueOf(previewText.charAt(i)));
        }
        
        int startX = width / 2 - totalWidth / 2;
        int currentX = startX;
        
        // Draw each character with its interpolated color
        for (int i = 0; i < messageLength; i++) {
            char c = previewText.charAt(i);
            
            // Calculate which segment this character belongs to
            double segmentIndex = i / charsPerSegment;
            int startColorIndex = (int) Math.floor(segmentIndex);
            int endColorIndex = Math.min(startColorIndex + 1, colors.length - 1);
            
            // Calculate interpolation factor
            double factor = segmentIndex - startColorIndex;
            
            // Get interpolated color
            int color = interpolateColor(colors[startColorIndex], colors[endColorIndex], factor);
            
            // Draw the character with the calculated color
            context.drawText(textRenderer, String.valueOf(c), currentX, gradientY, 0xFF000000 | color, true);
            
            // Move to the next character position
            currentX += textRenderer.getWidth(String.valueOf(c));
        }
        
        super.render(context, mouseX, mouseY, delta);
        
        // Draw tooltips for color buttons
        for (int i = 0; i < colorButtons.size(); i++) {
            ColorButton button = colorButtons.get(i);
            if (button.isHovered()) {
                int color = gradientManager.getPresets().get(selectedPreset).getColors()[i];
                String hexColor = String.format("#%06X", color);
                context.drawTooltip(textRenderer, Text.literal(hexColor), mouseX, mouseY);
            }
        }
    }
    
    @Override
    public void close() {
        try {
            // Save settings when closing
            gradientManager.setCurrentPresetIndex(selectedPreset);
            gradientManager.savePresets();
            MinecraftClient.getInstance().setScreen(null);
        } catch (Exception e) {
            // Log the error but don't crash
            System.err.println("Error closing GradientScreen: " + e.getMessage());
            // Fallback to just closing the screen
            MinecraftClient.getInstance().setScreen(null);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // Escape key
            close();
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
    
    // Helper method to interpolate between two colors
    private int interpolateColor(int color1, int color2, double factor) {
        Color c1 = new Color(color1);
        Color c2 = new Color(color2);
        
        int red = (int) (c1.getRed() * (1 - factor) + c2.getRed() * factor);
        int green = (int) (c1.getGreen() * (1 - factor) + c2.getGreen() * factor);
        int blue = (int) (c1.getBlue() * (1 - factor) + c2.getBlue() * factor);
        
        return new Color(red, green, blue).getRGB() & 0xFFFFFF; // Remove alpha
    }
    
    private static class ColorButton extends ButtonWidget {
        private int color;
        
        public ColorButton(int x, int y, int width, int height, Text message, PressAction onPress, int color) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.color = color;
        }
        
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            // Draw color
            context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000 | color);
            
            // Draw border
            context.drawBorder(getX(), getY(), width, height, isHovered() ? 0xFFFFFFFF : 0xAA888888);
        }
        
        public void setColor(int color) {
            this.color = color;
        }
    }
}