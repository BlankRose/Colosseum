package dev.blankrose.colosseum;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue CUSTOM_VANILLA_BOSS_MUSIC_ENABLED = BUILDER
        .comment("Whether does a custom music is played during boss fights")
        .define("enable_custom_boss_music", true);

    public static final ModConfigSpec.BooleanValue BOSS_RUSH_MUSIC_ENABLED = BUILDER
        .comment("Whether a custom track is played during the boss rush")
        .define("enable_boss_rush_music", true);

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    private static boolean validateSound(final Object obj) {
        return obj instanceof String soundName && BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.parse(soundName));
    }
}
