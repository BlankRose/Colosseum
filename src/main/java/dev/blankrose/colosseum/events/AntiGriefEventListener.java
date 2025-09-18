package dev.blankrose.colosseum.events;

import dev.blankrose.colosseum.effects.EffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/// Class responsible for listening to block-related events
/// and cancel then when an entity is under the effect of the
/// Anti Grief effect
public class AntiGriefEventListener {
    @SubscribeEvent
    public void breakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasEffect(EffectRegistry.ANTI_GRIEF_EFFECT)
            && !player.isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void placeEvent(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player
            && player.hasEffect(EffectRegistry.ANTI_GRIEF_EFFECT)
            && !player.isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void griefEvent(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living_entity
            && living_entity.hasEffect(EffectRegistry.ANTI_GRIEF_EFFECT)) {
            event.setCanGrief(false);
        }
    }
}
