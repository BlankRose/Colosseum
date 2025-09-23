package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import dev.blankrose.colosseum.ItemRegistry;
import dev.blankrose.colosseum.core.BossRush;
import dev.blankrose.colosseum.sounds.MusicPlayer;
import dev.blankrose.colosseum.sounds.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AltarEntity extends BlockEntity {
    public AltarEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ALTAR.get(), pos, blockState);
        this.handler = new BossRushAltar(this);
    }

    // --- HANDLER --- //

    public static class BossRushAltar extends BossRush {
        AltarEntity altar;

        public BossRushAltar(AltarEntity altar) {
            super(altar.getLevel(), altar.getBlockPos());
            this.altar = altar;
        }

        @Override
        public Level getLevel() {
            return altar.getLevel();
        }

        @Override
        public BlockPos getBlockPos() {
            return altar.getBlockPos();
        }

        @Override
        protected void onComplete() {
            if (getLevel() instanceof ServerLevel server_level) {
                Colosseum.LOGGER.info("Completed!");
                final Vec3 pos = getPos();
                final ItemStack reward = ItemRegistry.ETERNITY_SHARD.toStack(1);

                final Entity entity = new ItemEntity(server_level, pos.x, pos.y + 1.f, pos.z, reward, 0.f, .25f, 0.f);
                entity.setInvulnerable(true);
                server_level.addFreshEntity(entity);

                onEnd();
            }
        }

        @Override
        protected void onEnd() {
            Colosseum.LOGGER.info("Ended!");
            if (getLevel() instanceof ServerLevel server_level
                && altar.getBlockState().getValue(BlockStateProperties.ENABLED)) { // <-- prevents inf loop
                server_level.setBlockAndUpdate(getBlockPos(), altar.getBlockState().setValue(BlockStateProperties.ENABLED, false));
            }
            super.onEnd();
        }

        @Override
        protected void onSpawnSuccess() {
            if (!(getLevel() instanceof ServerLevel server_level)) {
                return;
            }

            Colosseum.LOGGER.info("Spawning Success");

            EntityType.LIGHTNING_BOLT.spawn(server_level, getBlockPos().above(), MobSpawnType.MOB_SUMMONED);
            server_level.playSound(null, getBlockPos(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 100.0f, 1.0f);
            server_level.setBlockAndUpdate(getBlockPos(), altar.getBlockState().setValue(BlockStateProperties.ENABLED, true));

            getPlayers().forEach(player -> {
                if (player instanceof ServerPlayer server_player) {
                    server_player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§cWave " + wave)));
                }
            });
        }
    }

    public final BossRushAltar handler;

    // --- FUNCTIONS --- //

    public static boolean IsNear(Level level, BlockPos pos) {
        return level.getNearbyPlayers(
            TargetingConditions.forNonCombat(),
            null,
            AABB.ofSize(pos.getCenter(), 50.0, 50.0, 50.0)
        ).stream().anyMatch(Player::isLocalPlayer);
    }

    @Override
    public void onChunkUnloaded() {
        this.handler.finish();
        super.onChunkUnloaded();
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
            if (block_entity instanceof AltarEntity altar_entity) {
                altar_entity.handler.onTick();
            }
        }
    }
}
