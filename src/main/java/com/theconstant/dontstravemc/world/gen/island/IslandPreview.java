package com.theconstant.dontstravemc.world.gen.island;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把当前岛屿布局画成俯视图。运行：{@code java ... IslandPreview <种子> <输出png>}
 */
public final class IslandPreview {

    private static final int BLOCKS_PER_PIXEL = 4;
    private static final int RADIUS_BLOCKS = 2600;
    private static final int MAP = RADIUS_BLOCKS * 2 / BLOCKS_PER_PIXEL;

    private IslandPreview() {
    }

    public static void main(String[] args) throws Exception {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 1L;
        Path output = Path.of(args.length > 1 ? args[1] : "build/island-preview.png");
        IslandLayout.bind(seed);
        IslandLayout layout = IslandLayout.get();

        BufferedImage image = new BufferedImage(MAP + 220, MAP, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(12, 32, 56));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        Map<IslandBiome, Integer> counts = new LinkedHashMap<>();
        for (int py = 0; py < MAP; py++) {
            int blockZ = (py - MAP / 2) * BLOCKS_PER_PIXEL;
            for (int px = 0; px < MAP; px++) {
                int blockX = (px - MAP / 2) * BLOCKS_PER_PIXEL;
                IslandLayout.Sample sample = layout.sample(blockX, blockZ);
                image.setRGB(px, py, color(sample));
                if (sample.land() || sample.biome() == IslandBiome.BEACH) {
                    counts.merge(sample.biome(), 1, Integer::sum);
                }
            }
        }

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        int legendY = 24;
        graphics.setColor(Color.WHITE);
        graphics.drawString("seed " + seed, MAP + 16, legendY);
        legendY += 28;
        for (IslandBiome biome : IslandBiome.values()) {
            graphics.setColor(new Color(flatColor(biome)));
            graphics.fillRect(MAP + 16, legendY - 10, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.drawString(biome.name().toLowerCase(), MAP + 36, legendY + 2);
            legendY += 20;
        }
        graphics.dispose();

        if (output.getParent() != null) {
            java.nio.file.Files.createDirectories(output.getParent());
        }
        ImageIO.write(image, "png", output.toFile());
        System.out.println("Wrote " + output.toAbsolutePath());
    }

    private static int color(IslandLayout.Sample sample) {
        int base = flatColor(sample.biome());
        if (!sample.land() && sample.biome() != IslandBiome.BEACH) {
            return base;
        }
        if (!sample.land()) {
            return scale(base, 0.82);
        }
        double shade = MthClamp((sample.surfaceY() - 64) / 90.0);
        double light = 0.55 + shade * 0.55;
        return scale(base, light);
    }

    private static double MthClamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static int scale(int rgb, double light) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * light));
        int g = Math.min(255, (int) (((rgb >> 8) & 255) * light));
        int b = Math.min(255, (int) ((rgb & 255) * light));
        return (r << 16) | (g << 8) | b;
    }

    private static int flatColor(IslandBiome biome) {
        return switch (biome) {
            case OCEAN -> 0x2F6F92;
            case DEEP_OCEAN -> 0x173A5C;
            case BEACH -> 0xE6D3A1;
            case ENTRY_FOREST -> 0x3E8F3A;
            case EVERGREEN_FOREST -> 0x1E6A3C;
            case GRASSLAND -> 0x8FCB55;
            case BIRCH_FOREST -> 0xA8C96A;
            case PIG_VILLAGE_FOREST -> 0x4F9144;
            case BEEFALO_SAVANNA -> 0xC4A15A;
            case SAVANNA -> 0xD2B16A;
            case DESERT -> 0xE4C57A;
            case ROCKYLAND -> 0x8E8680;
            case SWAMP -> 0x4C6234;
            case MERM_SWAMP -> 0x3E5C42;
            case WALRUS_FOREST -> 0x6E9480;
            case CEMETERY_FOREST -> 0x5E6E58;
            case LUNAR_FOREST -> 0xA9BFD4;
            case MANDRAKE_FOREST -> 0x6AAA4C;
            case METEOR_PLAIN -> 0x7A655C;
            case POND_FOREST -> 0x5A9A72;
        };
    }
}
