package dev.blankrose.colosseum;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    private static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Colosseum.MOD_ID);

    public static final DeferredItem<Item> MUSIC_DISC_BOSS_RUSH = REGISTRY.registerSimpleItem("music_disc_boss_rush", new Item.Properties()
        .jukeboxPlayable(SoundRegistry.BOSS_RUSH_MUSIC_KEY)
        .rarity(Rarity.EPIC)
        .stacksTo(1));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
