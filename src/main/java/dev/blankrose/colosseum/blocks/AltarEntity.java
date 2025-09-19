package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import dev.blankrose.colosseum.attachments.AttachmentRegistry;
import dev.blankrose.colosseum.attachments.ExtendedBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class AltarEntity extends BlockEntity {
    public AltarEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ALTAR.get(), pos, blockState);
        this.wave = 0;
        this.boss = null;
    }

    // --- HANDLES --- //

    /// TODO: Move this in a .json file and read at some point later to make it easier instead of this which need to be recompiled...
    private static final String[] RAW_BOSS_LIST = {
        "minecraft:zombie",
        "minecraft:elder_guardian",
        "minecraft:wither",
        "minecraft:ender_dragon",
        "minecraft:warden",
        "rats:rat_king"
    };

    public static boolean is_checked = false;
    public static EntityType<?>[] BOSS_LIST = {};

    /// Should be called once registries are frozen
    public static void checkBossList() {
        // No point in re-checking
        if (is_checked) { return; }

        BOSS_LIST = Arrays.stream(RAW_BOSS_LIST)
            .map(it -> {
                ResourceLocation resource_location = ResourceLocation.tryParse(it);
                if (!Objects.isNull(resource_location)
                    && BuiltInRegistries.ENTITY_TYPE.containsKey(resource_location)) {
                    return BuiltInRegistries.ENTITY_TYPE.get(resource_location);
                }
                Colosseum.LOGGER.info("This boss requires external dependencies to fight: {}", it);
                return null;
            })
            .filter(Objects::nonNull)
            .toArray(EntityType<?>[]::new);

        is_checked = true;
    }

    @Nullable
    public EntityType<?> getBoss(int index) {
        if (index < 0 || index >= BOSS_LIST.length) {
            return null;
        }
        return BOSS_LIST[index];
    }

    public void next_wave() {
        if (!is_checked
            || !(getLevel() instanceof ServerLevel server_level)) {
            return;
        }

        final EntityType<?> next_boss = this.getBoss(this.wave);
        if (Objects.isNull(next_boss)) {
            end();
            return;
        }
        this.wave += 1;
        Colosseum.LOGGER.info("Next Wave: {}", next_boss);

        Entity next = next_boss.spawn(server_level, getBlockPos().above(3), MobSpawnType.MOB_SUMMONED);
        if (Objects.isNull(next)) {
            Colosseum.LOGGER.warn("Couldn't spawn {}.. skipping..", next_boss);
            next_wave();
            return;
        }
        if (!(next instanceof LivingEntity living_entity)) {
            Colosseum.LOGGER.warn("{} is not a living entity.. skipping..", next_boss);
            next.kill();
            next_wave();
            return;
        }
        this.boss = living_entity;

        if (this.boss instanceof EnderDragon dragon) {
            dragon.setFightOrigin(getBlockPos().above(2));
        }

        EntityType.LIGHTNING_BOLT.spawn(server_level, getBlockPos().above(), MobSpawnType.MOB_SUMMONED);
        server_level.playSound(null, getBlockPos(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 100.0f, 1.0f);
        this.boss.setData(AttachmentRegistry.POSITION, new ExtendedBlockPos(getBlockPos(), server_level));

        server_level
            .getNearbyPlayers(TargetingConditions.forNonCombat(), null, AABB.ofSize(getBlockPos().getCenter(), 50.0, 50.0, 50.0))
            .forEach(player -> {
                if (player instanceof ServerPlayer server_player) {
                    server_player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§c").append(this.boss.getName())));
                }
            });
    }

    public void end() {
        if (Objects.nonNull(this.boss)) {
            this.boss.kill();
            this.boss = null;
        }
        this.wave = 0;
    }

    private static int tick = 0;
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (tick < 10) {
            tick += 1;
            return;
        }
        tick = 0;

        BlockEntity block_entity = level.getBlockEntity(blockPos);
        if (!(block_entity instanceof AltarEntity altar_entity)) { return; }

        if (Objects.nonNull(altar_entity.boss)
            && !altar_entity.boss.isAlive()) {
            altar_entity.boss = null;
            altar_entity.next_wave();
        }
    }

    // --- STORAGE --- //

    /// Index of current wave
    public int wave;
    /// Currently alive enemies
    public LivingEntity boss;

    // --- SAVE & LOAD --- //

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (Objects.nonNull(boss)) {
            tag.putUUID("boss", this.boss.getUUID());
            Colosseum.LOGGER.info("Non null - saving UUID");
        } else {
            Colosseum.LOGGER.info("Inexistant smh");
        }
        tag.putInt("wave", this.wave);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.wave = tag.getInt("wave");
        if (tag.hasUUID("boss")) {
            final UUID uuid = tag.getUUID("boss");
            final Level level = super.getLevel();
            if (Objects.nonNull(level)) {
                Entity found = level.getEntities((Entity) null, AABB.INFINITE, entity -> entity.getUUID() == uuid).getFirst();
                if (found instanceof LivingEntity living_entity) {
                    this.boss = living_entity;
                }
            }
        }
    }
}
