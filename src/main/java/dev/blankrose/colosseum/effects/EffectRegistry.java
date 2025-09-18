package dev.blankrose.colosseum.effects;

import dev.blankrose.colosseum.Colosseum;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectRegistry {
    private static final DeferredRegister<MobEffect> REGISTRY =
        DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Colosseum.MOD_ID);

    public static final Holder<MobEffect> ANTI_GRIEF_EFFECT = REGISTRY.register("anti_grief", AntiGriefEffect::new);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
