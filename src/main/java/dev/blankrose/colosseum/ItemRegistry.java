package dev.blankrose.colosseum;

import dev.blankrose.colosseum.sounds.SoundRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    private static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Colosseum.MOD_ID);

    public static final DeferredItem<Item> MUSIC_DISC_BOSS_RUSH = REGISTRY.registerSimpleItem("music_disc_boss_rush", new Item.Properties()
        .jukeboxPlayable(SoundRegistry.BOSS_RUSH_MUSIC_KEY)
        .rarity(Rarity.EPIC)
        .stacksTo(1));
    public static final DeferredItem<Item> MUSIC_DISC_LAP4 = REGISTRY.registerSimpleItem("music_disc_lap4", new Item.Properties()
        .jukeboxPlayable(SoundRegistry.LAP4_MUSIC_KEY)
        .rarity(Rarity.EPIC)
        .stacksTo(1));
    public static final DeferredItem<Item> MUSIC_DISC_LAP7 = REGISTRY.registerSimpleItem("music_disc_lap7", new Item.Properties()
        .jukeboxPlayable(SoundRegistry.LAP7_MUSIC_KEY)
        .rarity(Rarity.EPIC)
        .stacksTo(1));

    public static final DeferredItem<Item> ETERNITY_SHARD = REGISTRY.registerSimpleItem("eternity_shard", new Item.Properties()
        .rarity(Rarity.EPIC)
        .fireResistant());

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static <T extends Block> void registerBlockAsItem(DeferredBlock<T> block) {
        REGISTRY.registerSimpleBlockItem(block);
    }
}
