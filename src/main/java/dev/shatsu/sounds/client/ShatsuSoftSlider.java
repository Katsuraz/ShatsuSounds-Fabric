package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.DoubleConsumer;

/** Custom minimalist slider with actual drag and click feedback. */
public final class ShatsuSoftSlider extends AbstractWidget {
    private final String title;
    private final float min;
    private final float max;
    private final boolean pitch;
    private final DoubleConsumer update;
    private float current;

    public ShatsuSoftSlider(int x, int y, int width, String title, float current,
                            float min, float max, boolean pitch, DoubleConsumer update) {
        super(x, y, Math.max(35, width), 25, Component.literal(title));
        this.title = title;
        this.min = min;
        this.max = max;
        this.pitch = pitch;
        this.update = update;
        this.current = Math.max(min, Math.min(max, current));
    }

    private void change(double mouseX) {
        double t = (mouseX - (getX() + 6)) / Math.max(1, getWidth() - 12);
        t = Math.max(0, Math.min(1, t));
        current = (float)(min + t * (max - min));
        update.accept(current);
    }

    @Override public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (active) change(event.x());
    }

    @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (active) change(event.x());
    }

    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x = getX(), y = getY(), w = getWidth();
        int activeText = 0xFFF0E7F6, mutedText = 0xFF9D90AE;
        g.text(Minecraft.getInstance().font, title, x + 2, y, activeText);
        String val = pitch ? String.format(Locale.ROOT, "%.2fx", current)
                           : Math.round(current * 100) + "%";
        int valW = Minecraft.getInstance().font.width(val);
        g.text(Minecraft.getInstance().font, val, x + w - valW - 2, y, mutedText);
        int trackY = y + 19;
        g.fill(x + 5, trackY, x + w - 5, trackY + 3, 0xFF3E354A);
        float fraction = (current - min) / (max - min);
        int filled = (int)((w - 10) * fraction);
        if (filled > 0)
            g.fill(x + 5, trackY, x + 5 + filled, trackY + 3, 0xFFC3A3F2);
        int handleX = x + 5 + filled;
        g.fill(handleX - 2, trackY - 3, handleX + 3, trackY + 6,
            isHoveredOrFocused() ? 0xFFF5E5FF : 0xFFE2CBFA);
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(title));
    }
}
