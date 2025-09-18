package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import dev.blankrose.colosseum.ItemRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegistry {
    private static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(Colosseum.MOD_ID);

    public static final DeferredBlock<Block> ALTAR = newBlock("altar",
        () -> new Altar(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noOcclusion()));

    private static <T extends Block> DeferredBlock<T> newBlock(String name, Supplier<T> supplier) {
        DeferredBlock<T> registered = REGISTRY.register(name, supplier);
        ItemRegistry.registerBlockAsItem(registered);
        return registered;
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
