package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    private static final DeferredRegister<BlockEntityType<?>> REGISTRY =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Colosseum.MOD_ID);

    public static final Supplier<BlockEntityType<AltarEntity>> ALTAR = REGISTRY.register(
        "altar",
        () -> BlockEntityType.Builder.of(
            AltarEntity::new,
            BlockRegistry.ALTAR.get()
        ).build(null)
    );

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
