package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Read-only live sound diagnostics. A sound listed in sounds.json is NOT
 * proof Clangor or Minecraft actually invokes it.
 *
 * Reflection allows this source to compile with the repository's older food
 * module while inspecting the newer food hooks in the integrated alpha JAR.
 */
public final class ShatsuAudioHealth {
    public record Snapshot(
        boolean clangorInstalled, int registeredSwings, int declaredSwings,
        int inventoryClicks, int inventoryMissing,
        String drinkingLocal, String drinkingRemote, String drinkingMissing,
        String drinkingEnabled, String lastFood, String interpretation
    ) {}

    private ShatsuAudioHealth() {}

    private static String staticFieldMethod(String className, String method) {
        try {
            Class<?> c = Class.forName(className);
            Object result = c.getMethod(method).invoke(null);
            return String.valueOf(result);
        } catch (ReflectiveOperationException | LinkageError ex) {
            return "unavailable";
        }
    }

    private static String boolConfig(String name) {
        try {
            Object result = ShatsuConfig.current().getClass().getField(name)
                .get(ShatsuConfig.current());
            return Boolean.TRUE.equals(result) ? "enabled" : "disabled";
        } catch (ReflectiveOperationException | LinkageError ex) {
            return "unknown";
        }
    }

    public static Snapshot scan() {
        Minecraft client = Minecraft.getInstance();
        boolean clangor = FabricLoader.getInstance().isModLoaded("clangor");
        int total = 0, registered = 0;
        for (ShatsuCatalog.Entry entry : ShatsuCatalog.all()) {
            if (!entry.id().startsWith("clangor:swing.")) continue;
            total++;
            try {
                Identifier id = Identifier.parse(entry.id());
                var event = client.getSoundManager().getSoundEvent(id);
                if (event != null && event.getWeight() > 0) registered++;
            } catch (RuntimeException ex) {
                // Invalid event ID: show unregistered rather than crashing UI.
            }
        }
        String local = staticFieldMethod("dev.shatsu.sounds.client.ShatsuFoodAudio", "localMatches");
        String remote = staticFieldMethod("dev.shatsu.sounds.client.ShatsuFoodAudio", "remoteMatches");
        String missing = staticFieldMethod("dev.shatsu.sounds.client.ShatsuFoodAudio", "missingEvents");
        String last = staticFieldMethod("dev.shatsu.sounds.client.ShatsuFoodAudio", "lastProfile");
        String interpretation = !clangor
            ? "Clangor isn't installed; swing resources alone won't trigger it."
            : registered == 0
                ? "No registered swing sounds. Check sound packs and F3+T."
                : "Swing resources found. Test a real swing to verify Clangor.";
        return new Snapshot(clangor, registered, total,
            ShatsuInventoryAudio.interactions(), ShatsuInventoryAudio.missingEvents(),
            local, remote, missing, boolConfig("drinkAudioEnabled"), last, interpretation);
    }
}
