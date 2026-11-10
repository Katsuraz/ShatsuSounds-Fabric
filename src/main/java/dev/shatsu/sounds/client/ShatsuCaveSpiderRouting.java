package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.phys.AABB;

/** Locally replaces only a Cave Spider's ordinary Spider sound packet.
 *  Never adds a second layer: mixins cancel the original packet only when
 *  a specific Cave Spider can be identified with reasonable certainty.
 */
public final class ShatsuCaveSpiderRouting {
    private static final String VANILLA_PREFIX = "entity.spider.";
    private ShatsuCaveSpiderRouting() {}

    private static Identifier replacement(SoundEvent original) {
        Identifier old = BuiltInRegistries.SOUND_EVENT.getKey(original);
        if (old == null || !"minecraft".equals(old.getNamespace())) return null;
        String event = old.getPath();
        if (!event.startsWith(VANILLA_PREFIX)) return null;
        String name = event.substring(VANILLA_PREFIX.length());
        if (!name.equals("ambient") && !name.equals("hurt")
                && !name.equals("death") && !name.equals("step")) return null;
        return Identifier.fromNamespaceAndPath("shatsufresh", "cave_spider.preview." + name);
    }

    /** Handle packets that explicitly reference the entity ID. */
    public static boolean entitySound(int entityId, SoundEvent sound, SoundSource source,
            float volume, float pitch) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null) return false;
        Entity entity = world.getEntity(entityId);
        if (!(entity instanceof CaveSpider)) return false;
        Identifier target = replacement(sound);
        if (target == null || client.getSoundManager().getSoundEvent(target) == null) return false;
        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
            SoundEvent.createVariableRangeEvent(target), source, volume, pitch, false);
        return true;
    }

    /** Handle ordinary positional packets by tightly matching their exact origin.
     *  If a normal Spider is equally close, keep the original rather than guess.
     */
    public static boolean positionedSound(double x, double y, double z,
            SoundEvent sound, SoundSource source, float volume, float pitch) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null) return false;
        Identifier target = replacement(sound);
        if (target == null || client.getSoundManager().getSoundEvent(target) == null) return false;

        AABB region = new AABB(x - .65, y - .65, z - .65, x + .65, y + .65, z + .65);
        java.util.List<Spider> nearby = world.getEntitiesOfClass(Spider.class, region);
        CaveSpider choice = null;
        double best = .36; // squared 0.6 block distance for exact sound origin
        for (Spider spider : nearby) {
            double distance = spider.distanceToSqr(x, y, z);
            if (distance >= best) continue;
            if (spider instanceof CaveSpider candidate) {
                choice = candidate;
                best = distance;
            } else {
                return false; // never hijack an ordinary spider's sound
            }
        }
        if (choice == null) return false;
        world.playLocalSound(x, y, z, SoundEvent.createVariableRangeEvent(target),
            source, volume, pitch, false);
        return true;
    }
}
