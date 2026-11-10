package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Custom keyboard-focusable Shatsu control. No vanilla button sprites or textures.
 * All styling is drawn through 26.3's GuiGraphicsExtractor.
 */
public final class ShatsuSoftButton extends AbstractWidget {
    private final Runnable command;
    private final boolean accent;

    public ShatsuSoftButton(int x, int y, int width, int height, Component title,
                            Runnable command, boolean accent) {
        super(x, y, Math.max(20, width), Math.max(18, height), title);
        this.command = command;
        this.accent = accent;
    }

    private static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        int r = Math.min(5, Math.max(2, h / 5));
        g.fill(x + r, y, x + w - r, y + h, c);
        g.fill(x, y + r, x + w, y + h - r, c);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, c);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        boolean hovered = isHoveredOrFocused();
        int bg = !active ? 0x80332B42 :
                 accent ? (hovered ? 0xFFB19AE3 : 0xFF8D72BC) :
                 hovered ? 0xFF514364 : 0xFF332D41;
        int fg = !active ? 0xFF80768C : accent ? 0xFF171022 : 0xFFF3EDFA;
        if (hovered && active)
            rounded(g, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0xA66B5988);
        rounded(g, getX(), getY(), getWidth(), getHeight(), bg);
        if (getWidth() > 80) {
            g.text(Minecraft.getInstance().font, getMessage(),
                getX() + 10, getY() + (getHeight() - 8) / 2, fg);
        } else {
            g.centeredText(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, fg);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (active && visible) command.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
