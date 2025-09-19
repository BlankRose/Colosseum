package dev.blankrose.colosseum.attachments;

import dev.blankrose.colosseum.Colosseum;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachmentRegistry {
    private static final DeferredRegister<AttachmentType<?>> REGISTRY =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Colosseum.MOD_ID);

    public static final Supplier<AttachmentType<ExtendedBlockPos>> POSITION = REGISTRY.register(
        "position",
        () -> AttachmentType.builder(ExtendedBlockPos::new)
            .serialize(new ExtendedBlockPos.Serializer())
            .copyOnDeath()
            .build()
    );

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
