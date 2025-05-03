package com.example.gradientchat;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

public class GradientManager {
    private static final int NUM_PRESETS = 5;
    private static final int COLORS_PER_PRESET = 10;
    
    private final List<GradientPreset> presets;
    private int currentPresetIndex;
    
    public GradientManager() {
        presets = new ArrayList<>();
        currentPresetIndex = 0;
        
        // Initialize with default presets
        initializeDefaultPresets();
        
        // Load saved presets if available
        loadPresets();
    }
    
    private void initializeDefaultPresets() {
        // Preset 1: Red Gradient
        presets.add(new GradientPreset("🔥 Red Gradient (dark to light)", new int[] {
                0x8B0000, 0xA60000, 0xC80000, 0xE00000, 0xFF0000,
                0xFF4040, 0xFF7373, 0xFF9999, 0xFF0000, 0x8B0000
        }));
        
        // Preset 2: Blue Gradient
        presets.add(new GradientPreset("🔵 Blue Gradient (deep to sky)", new int[] {
                0x00008B, 0x0000CD, 0x1E90FF, 0x00BFFF, 0x87CEFA,
                0xB0E0E6, 0xE0FFFF, 0xF0FFFF, 0x00BFFF, 0x00008B
        }));
        
        // Preset 3: Green Gradient
        presets.add(new GradientPreset("💚 Green Gradient (forest to lime)", new int[] {
                0x006400, 0x228B22, 0x2E8B57, 0x3CB371, 0x66CDAA,
                0x7FFFD4, 0x98FB98, 0xADFF2F, 0x3CB371, 0x006400
        }));
        
        // Preset 4: Purple Gradient
        presets.add(new GradientPreset("🟣 Purple Gradient (rich to light)", new int[] {
                0x4B0082, 0x6A0DAD, 0x800080, 0xA020F0, 0xDA70D6,
                0xD8BFD8, 0xE6E6FA, 0xF8F8FF, 0xA020F0, 0x4B0082
        }));
        
        // Preset 5: Rainbow
        presets.add(new GradientPreset("🌈 Rainbow (hard steps, not blended)", new int[] {
                0xFF0000, 0xFF7F00, 0xFFFF00, 0x00FF00, 0x0000FF,
                0x4B0082, 0x8B00FF, 0xFF0000, 0xFF7F00, 0xFFFF00
        }));
    }
    
    public void savePresets() {
        try {
            File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "gradient-chat");
            if (!configDir.exists() && !configDir.mkdirs()) {
                GradientChatMod.LOGGER.error("Failed to create config directory");
                return;
            }
            
            File presetsFile = new File(configDir, "presets.dat");
            
            NbtCompound root = new NbtCompound();
            root.putInt("currentPreset", currentPresetIndex);
            
            NbtList presetsList = new NbtList();
            for (GradientPreset preset : presets) {
                NbtCompound presetNbt = new NbtCompound();
                presetNbt.putString("name", preset.getName());
                
                NbtList colorsList = new NbtList();
                for (int color : preset.getColors()) {
                    NbtCompound colorNbt = new NbtCompound();
                    colorNbt.putInt("value", color);
                    colorsList.add(colorNbt);
                }
                
                presetNbt.put("colors", colorsList);
                presetsList.add(presetNbt);
            }
            
            root.put("presets", presetsList);
            
            NbtIo.write(root, presetsFile.toPath());
        } catch (IOException e) {
            GradientChatMod.LOGGER.error("Failed to save presets", e);
        }
    }
    
    public void loadPresets() {
        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "gradient-chat");
        File presetsFile = new File(configDir, "presets.dat");
        
        if (!presetsFile.exists()) {
            return;
        }
        
        try {
            NbtCompound root = NbtIo.read(presetsFile.toPath());
            if (root == null) {
                return;
            }
            
            currentPresetIndex = root.getInt("currentPreset");
            
            NbtList presetsList = root.getList("presets", 10); // 10 is the NBT type for compound tags
            if (presetsList.size() == NUM_PRESETS) {
                presets.clear();
                
                for (int i = 0; i < presetsList.size(); i++) {
                    NbtCompound presetNbt = presetsList.getCompound(i);
                    String name = presetNbt.getString("name");
                    
                    NbtList colorsList = presetNbt.getList("colors", 10);
                    int[] colors = new int[COLORS_PER_PRESET];
                    
                    for (int j = 0; j < COLORS_PER_PRESET; j++) {
                        colors[j] = colorsList.getCompound(j).getInt("value");
                    }
                    
                    presets.add(new GradientPreset(name, colors));
                }
            }
        } catch (IOException e) {
            GradientChatMod.LOGGER.error("Failed to load presets", e);
        }
    }
    
    public GradientPreset getCurrentPreset() {
        return presets.get(currentPresetIndex);
    }
    
    public void setCurrentPresetIndex(int index) {
        if (index >= 0 && index < presets.size()) {
            currentPresetIndex = index;
            savePresets();
        }
    }
    
    public List<GradientPreset> getPresets() {
        return presets;
    }
    
    public String applyGradient(String message) {
        if (message.isEmpty()) {
            return message;
        }
        
        GradientPreset preset = getCurrentPreset();
        int[] colors = preset.getColors();
        
        StringBuilder result = new StringBuilder();
        
        // Calculate how many characters per color segment
        int messageLength = message.length();
        double charsPerSegment = (double) messageLength / (colors.length - 1);
        
        for (int i = 0; i < messageLength; i++) {
            char c = message.charAt(i);
            
            // Calculate which segment this character belongs to
            double segmentIndex = i / charsPerSegment;
            int startColorIndex = (int) Math.floor(segmentIndex);
            int endColorIndex = Math.min(startColorIndex + 1, colors.length - 1);
            
            // Calculate interpolation factor
            double factor = segmentIndex - startColorIndex;
            
            // Get interpolated color
            int color = interpolateColor(colors[startColorIndex], colors[endColorIndex], factor);
            
            // Format: #RRGGBB
            result.append(formatHexColor(color));
            result.append(c);
        }
        
        return result.toString();
    }
    
    private int interpolateColor(int color1, int color2, double factor) {
        Color c1 = new Color(color1);
        Color c2 = new Color(color2);
        
        int red = (int) (c1.getRed() * (1 - factor) + c2.getRed() * factor);
        int green = (int) (c1.getGreen() * (1 - factor) + c2.getGreen() * factor);
        int blue = (int) (c1.getBlue() * (1 - factor) + c2.getBlue() * factor);
        
        return new Color(red, green, blue).getRGB() & 0xFFFFFF; // Remove alpha
    }
    
    private String formatHexColor(int color) {
        // Format: #RRGGBB
        return String.format("#%06X", color);
    }
    
    public static class GradientPreset {
        private String name;
        private int[] colors;
        
        public GradientPreset(String name, int[] colors) {
            this.name = name;
            this.colors = colors;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int[] getColors() {
            return colors;
        }
        
        public void setColor(int index, int color) {
            if (index >= 0 && index < colors.length) {
                colors[index] = color;
            }
        }
    }
}