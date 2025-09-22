package dev.blankrose.colosseum.sounds;

import dev.blankrose.colosseum.Colosseum;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SoundRegistry {
    private static final DeferredRegister<SoundEvent> REGISTRY =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Colosseum.MOD_ID);

    public static final Supplier<SoundEvent> BOSS_RUSH_MUSIC = registerSoundEvent("boss_rush_music");
    public static final ResourceKey<JukeboxSong> BOSS_RUSH_MUSIC_KEY = createSong("boss_rush_music");

    public static final Supplier<SoundEvent> LAP4_MUSIC = registerSoundEvent("lap4_music");
    public static final ResourceKey<JukeboxSong> LAP4_MUSIC_KEY = createSong("lap4_music");

    public static final Supplier<SoundEvent> LAP7_MUSIC = registerSoundEvent("lap7_music");
    public static final ResourceKey<JukeboxSong> LAP7_MUSIC_KEY = createSong("lap7_music");

    private static ResourceKey<JukeboxSong> createSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(Colosseum.MOD_ID, name));
    }

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Colosseum.MOD_ID, name);
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
