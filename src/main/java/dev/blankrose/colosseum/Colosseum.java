package dev.blankrose.colosseum;

import dev.blankrose.colosseum.blocks.BlockEntityRegistry;
import dev.blankrose.colosseum.blocks.BlockRegistry;
import dev.blankrose.colosseum.effects.EffectRegistry;
import dev.blankrose.colosseum.events.AntiGriefEventListener;
import dev.blankrose.colosseum.events.PostRegistryNotifier;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Colosseum.MOD_ID)
public class Colosseum {
    public static final String MOD_ID = "colosseum";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Colosseum(IEventBus modEventBus, ModContainer modContainer) {
        EffectRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        CreativeTab.register(modEventBus);

        NeoForge.EVENT_BUS.register(new AntiGriefEventListener());
        modEventBus.register(new PostRegistryNotifier());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
