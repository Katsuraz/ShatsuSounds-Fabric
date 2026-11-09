package dev.shatsu.sounds.client;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Accessible, woodland-styled controls. Still a real Minecraft Button:
 * mouse/keyboard input, focus and narration all remain vanilla-compatible.
 */
public final class ShatsuButton extends Button {
    private enum Tone { STANDARD, SELECTED, PLAY, STOP, PRIMARY, QUIET }
    private final Tone tone;

    public ShatsuButton(int x, int y, int width, int height, String label, Runnable action) {
        super(x, y, width, height, Component.literal(label), b -> action.run(), DEFAULT_NARRATION);
        String name = label.trim().toLowerCase(Locale.ROOT);
        if (name.startsWith("•") || name.startsWith("●") || name.equals("on")) {
            this.tone = Tone.SELECTED;
        } else if (name.equals("play") || name.startsWith("play ") || name.startsWith("test ")) {
            this.tone = Tone.PLAY;
        } else if (name.startsWith("stop") || name.equals("off")) {
            this.tone = Tone.STOP;
        } else if (name.startsWith("save") || name.equals("find") || name.startsWith("browse ")) {
            this.tone = Tone.PRIMARY;
        } else if (name.equals("reset") || name.startsWith("vol ") || name.startsWith("pitch ")
                    || name.equals("default") || name.startsWith("< ") || name.endsWith(" >")) {
            this.tone = Tone.QUIET;
        } else {
            this.tone = Tone.STANDARD;
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX(), y = this.getY();
        int w = this.getWidth(), h = this.getHeight();
        boolean hover = this.isHoveredOrFocused();
        boolean enabled = this.isActive();

        int edge, upper, lower, stripe;
        switch (tone) {
            case SELECTED -> {
                edge = 0xFFC4A774; upper = 0xFF536955; lower = 0xFF34493E; stripe = 0xFFE8C88D;
            }
            case PLAY -> {
                edge = 0xFF678F73; upper = 0xFF3B6752; lower = 0xFF284B3B; stripe = 0xFFA2DBB0;
            }
            case STOP -> {
                edge = 0xFF906867; upper = 0xFF654341; lower = 0xFF4A3234; stripe = 0xFFF0B6A4;
            }
            case PRIMARY -> {
                edge = 0xFFA38A5D; upper = 0xFF695A3F; lower = 0xFF4A4434; stripe = 0xFFF0D39C;
            }
            case QUIET -> {
                edge = 0xFF52645E; upper = 0xFF2E3C37; lower = 0xFF24332F; stripe = 0xFF8FA89A;
            }
            default -> {
                edge = 0xFF61756A; upper = 0xFF364941; lower = 0xFF273C35; stripe = 0xFF9DB6A6;
            }
        }

        // One-pixel bevel, subtle gradient, active accent, hover and keyboard focus.
        graphics.fill(x, y, x + w, y + h, enabled ? edge : 0xFF404845);
        graphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1,
                enabled && hover ? lighten(upper, 23) : upper,
                enabled && hover ? lighten(lower, 18) : lower);
        if (h >= 18 && w >= 26) {
            graphics.fill(x + 2, y + 2, x + w - 2, y + 3, 0x527C9181);
            graphics.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, 0x5E101B18);
        }
        if (w >= 40) graphics.fill(x + 1, y + 1, x + 3, y + h - 1, stripe);
        if (hover && enabled && w > 4 && h > 4) {
            graphics.outline(x + 2, y + 2, w - 4, h - 4, 0x99E8D9B7);
        }
        this.extractDefaultLabel(graphics.textRendererForWidget(
                this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    private static int lighten(int rgb, int amount) {
        int red = Math.min(255, ((rgb >> 16) & 255) + amount);
        int green = Math.min(255, ((rgb >> 8) & 255) + amount);
        int blue = Math.min(255, (rgb & 255) + amount);
        return (rgb & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }
}
