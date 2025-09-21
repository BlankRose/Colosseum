package dev.blankrose.colosseum.loot;

import com.mojang.serialization.MapCodec;
import dev.blankrose.colosseum.Colosseum;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class LootModifierRegistry {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Colosseum.MOD_ID);

    public static final Supplier<MapCodec<VoidLootModifier>> VOID_LOOT_MODIFIER =
        REGISTRY.register("void_modifier", () -> VoidLootModifier.CODEC);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
