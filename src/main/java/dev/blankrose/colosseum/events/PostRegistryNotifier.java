package dev.blankrose.colosseum.events;

import dev.blankrose.colosseum.blocks.AltarEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/// Notifies classes which requires that the
/// registry becomes frozen before doing their tasks.
public class PostRegistryNotifier {
    @SubscribeEvent
    public void commonSetupEvent(FMLCommonSetupEvent event) {
        AltarEntity.checkBossList();
    }
}
