package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import dev.blankrose.colosseum.ItemRegistry;
import dev.blankrose.colosseum.attachments.AttachmentRegistry;
import dev.blankrose.colosseum.attachments.ExtendedBlockPos;
import dev.blankrose.colosseum.sounds.MusicPlayer;
import dev.blankrose.colosseum.sounds.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AltarEntity extends BlockEntity {
    public AltarEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ALTAR.get(), pos, blockState);
        this.wave = 0;
        this.boss = null;
    }

    // --- HANDLES --- //

    @Nullable
    public EntityType<?> getBoss(int index) {
        if (index < 0) { return null; }

        HolderSet.Named<EntityType<?>> list = BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(Tags.EntityTypes.BOSSES);
        if (list.size() <= index) { return null; }

        return list.get(index).value();
    }

    public void spawnNextWave() {
        if (!(getLevel() instanceof ServerLevel server_level)) {
            return;
        }

        final List<Player> players = getPlayers();
        if (players.isEmpty()) {
            end();
            return;
        }

        final EntityType<?> next_boss = this.getBoss(this.wave);
        if (Objects.isNull(next_boss)) {
            win();
            return;
        }
        this.wave += 1;

        Entity next = next_boss.spawn(server_level, getBlockPos().above(3), MobSpawnType.MOB_SUMMONED);
        if (Objects.isNull(next)) {
            spawnNextWave();
            return;
        }
        if (!(next instanceof LivingEntity living_entity)) {
            next.kill();
            spawnNextWave();
            return;
        }
        this.boss = living_entity;

        if (this.boss instanceof EnderDragon dragon) {
            dragon.setFightOrigin(getBlockPos().above(2));
        }

        EntityType.LIGHTNING_BOLT.spawn(server_level, getBlockPos().above(), MobSpawnType.MOB_SUMMONED);
        server_level.playSound(null, getBlockPos(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 100.0f, 1.0f);
        this.boss.setData(AttachmentRegistry.POSITION, new ExtendedBlockPos(getBlockPos(), server_level));
        this.boss.addTag("Summoned");

        server_level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockStateProperties.ENABLED, true));

        players.forEach(player -> {
            if (player instanceof ServerPlayer server_player) {
                server_player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§c").append(this.boss.getName())));
            }
        });
    }

    /// Checks if the summoned entity can be rid of
    public boolean check_despawn() {
        if (getPlayers().isEmpty()) {
            end();
            return true;
        }
        return false;
    }

    public List<Player> getPlayers() {
        Level world = getLevel();
        if (Objects.isNull(world)) {
            return List.of();
        }
        return world.getNearbyPlayers(
            TargetingConditions.forNonCombat(),
            null,
            AABB.ofSize(getBlockPos().getCenter(), 50.0, 50.0, 50.0)
        );
    }

    public static boolean IsNear(Level level, BlockPos pos) {
        return level.getNearbyPlayers(
            TargetingConditions.forNonCombat(),
            null,
            AABB.ofSize(pos.getCenter(), 50.0, 50.0, 50.0)
        ).stream().anyMatch(Player::isLocalPlayer);
    }

    @Override
    public void onChunkUnloaded() {
        end();
        super.onChunkUnloaded();
    }

    public void end() {
        if (getLevel() instanceof ServerLevel server_level
            && getBlockState().getValue(BlockStateProperties.ENABLED)) { // <-- prevents inf loop
            server_level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockStateProperties.ENABLED, false));
        }

        if (Objects.nonNull(this.boss)) {
            this.boss.kill();
            this.boss = null;
        }
        this.wave = 0;
    }

    public void win() {
        if (getLevel() instanceof ServerLevel server_level) {
            final Vec3 pos = getBlockPos().getCenter();
            final ItemStack reward = ItemRegistry.ETERNITY_SHARD.toStack(1);
            final Entity entity = new ItemEntity(server_level, pos.x, pos.y + 1.f, pos.z, reward, 0.f, .25f, 0.f);
            entity.setInvulnerable(true);
            server_level.addFreshEntity(entity);
            end();
        }
    }

    private static int tick = 0;
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState block_state, T t) {
        if (tick < 10) {
            tick += 1;
            return;
        }
        tick = 0;

        if (level.isClientSide) {
            if (!IsNear(level, blockPos) || !block_state.getValue(BlockStateProperties.ENABLED)) {
                MusicPlayer.stop();
            } else {
                MusicPlayer.select(SoundRegistry.BOSS_RUSH_MUSIC);
                MusicPlayer.play();
            }
        }
        else /* isServerSide */ {
            BlockEntity block_entity = level.getBlockEntity(blockPos);
            if (!(block_entity instanceof AltarEntity altar_entity)
                || altar_entity.check_despawn()) {
                return;
            }

            if (Objects.nonNull(altar_entity.boss)
                && !altar_entity.boss.isAlive()) {
                altar_entity.boss = null;
                altar_entity.spawnNextWave();
            }
        }
    }

    // --- STORAGE --- //

    public int wave;
    public LivingEntity boss;

    // --- SAVE & LOAD --- //

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (Objects.nonNull(boss)) {
            tag.putUUID("boss", this.boss.getUUID());
        }
        tag.putInt("wave", this.wave);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID("boss")) {
            this.wave = tag.getInt("wave");
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
