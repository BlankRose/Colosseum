package dev.blankrose.colosseum;

import dev.blankrose.colosseum.blocks.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTRY =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Colosseum.MOD_ID);

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = REGISTRY.register("creative_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.colosseum")) //The language key for the title of your CreativeModeTab
        .icon(Items.NETHERITE_SWORD::getDefaultInstance)
        .displayItems((parameters, output) -> {
            output.accept(BlockRegistry.ALTAR.get());
            output.accept(ItemRegistry.ETERNITY_SHARD.get());
            output.accept(ItemRegistry.MUSIC_DISC_BOSS_RUSH.get());
            output.accept(ItemRegistry.MUSIC_DISC_LAP4.get());
            output.accept(ItemRegistry.MUSIC_DISC_LAP7.get());
        }).build());

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
