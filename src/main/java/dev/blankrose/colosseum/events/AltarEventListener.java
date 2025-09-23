package dev.blankrose.colosseum.events;

import dev.blankrose.colosseum.attachments.AttachmentRegistry;
import dev.blankrose.colosseum.attachments.ExtendedBlockPos;
import dev.blankrose.colosseum.blocks.AltarEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.Objects;

public class AltarEventListener {
    @SubscribeEvent
    public void onServerStop(ServerStoppingEvent event) {
        event.getServer().getAllLevels().forEach((level) -> {
            level.getAllEntities().forEach(entity -> {
                if (Objects.nonNull(entity)
                    && entity.hasData(AttachmentRegistry.POSITION)) {

                    common(entity);
                }
            });
        });
    }

    @SubscribeEvent
    public void onDespawn(MobDespawnEvent event) {
        if (event.getEntity().hasData(AttachmentRegistry.POSITION)) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }

    private void common(Entity entity) {
        if (Objects.isNull(entity)
            || Objects.isNull(entity.getServer())
            || !entity.hasData(AttachmentRegistry.POSITION)) {
            return;
        }

        ExtendedBlockPos pos = entity.getData(AttachmentRegistry.POSITION);
        ServerLevel level = pos.getLevel(entity.getServer());
        if (Objects.isNull(level)
            || !(level.getBlockEntity(pos.getPos()) instanceof AltarEntity block_entity)) {
            return;
        }

        block_entity.handler.finish();
    }
}
