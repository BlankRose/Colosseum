package dev.blankrose.colosseum.core;

import dev.blankrose.colosseum.Colosseum;
import dev.blankrose.colosseum.attachments.AttachmentRegistry;
import dev.blankrose.colosseum.attachments.ExtendedBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractWaveChallenge {
    protected int wave;
    protected ArrayList<LivingEntity> active;
    protected Level world;
    protected BlockPos pos;
    protected AABB boundaries;

    public AbstractWaveChallenge(Level world, BlockPos pos, AABB boundaries) {
        this.wave = 0;
        this.active = new ArrayList<>();
        this.world = world;
        this.pos = pos;
        this.boundaries = boundaries;
    }

    public AbstractWaveChallenge(Level world, BlockPos pos) {
        this(world, pos, AABB.INFINITE);
    }

    /// This function shall retrieve all the entities that should spawn.
    /// Can have multiple of the same type if it should spawn multiple of it.
    @Nullable
    public abstract Collection<EntityType<?>> getWave(int index);

    /// This function shall indicate whether all waves been completed
    public abstract boolean isAllWavesComplete();

    public int getCurrentWave() {
        return wave;
    }

    public Level getLevel() {
        return world;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public Vec3 getPos() {
        return pos.getCenter();
    }

    public AABB getBoundaries() {
        return boundaries;
    }

    public boolean canTrigger(Player player) {
        return true;
    }

    public boolean isActive() {
        return this.wave > 0;
    }

    /// Simply retrieves the nearby players within the given `boundaries` specified
    public List<Player> getPlayers() {
        Level world = getLevel();
        if (Objects.isNull(world)) {
            return List.of();
        }
        return world.getNearbyPlayers(TargetingConditions.forNonCombat(), null, getBoundaries());
    }

    public void finish() {
        onEnd();
    }

    /// Checks if it can spawn the next wave
    protected boolean canSpawnNextWave() {
        Colosseum.LOGGER.info("A-1");
        if (checkDespawn()) {
            return false;
        }

        Colosseum.LOGGER.info("A-2");
        if (isAllWavesComplete()) {
            onComplete();
            return false;
        }

        return true;
    }

    /// Main function which handles the spawning of the next wave
    public void spawnNextWave() {
        Colosseum.LOGGER.info("0");
        if (!(getLevel() instanceof ServerLevel)) {
            return;
        }

        Colosseum.LOGGER.info("A");
        this.wave += 1;
        if (!canSpawnNextWave()) {
            return;
        }

        Colosseum.LOGGER.info("B");
        if (wave == 1) {
            onStart();
        }
        onWave();

        Colosseum.LOGGER.info("C");
        final Collection<EntityType<?>> next_targets = this.getWave(this.wave - 1);
        if (Objects.isNull(next_targets)) {
            spawnNextWave();
            return;
        }

        Colosseum.LOGGER.info("D");
        next_targets.stream()
            .map(this::spawnSingleEntity)
            .filter(Objects::nonNull)
            .forEach(active::add);

        Colosseum.LOGGER.info("E");
        if (active.isEmpty()) {
            spawnNextWave();
            return;
        }

        Colosseum.LOGGER.info("F");
        onSpawnSuccess();
    }

    /// Handles a single spawning entity. May yield `null` if it failed to spawn.
    ///
    /// Default implementation: Creates at given `level` and `pos` the specified `target`,
    /// then calls `applyOptionToEntity()` before spawning.
    @Nullable
    protected LivingEntity spawnSingleEntity(EntityType<?> target) {
        if (!(getLevel() instanceof ServerLevel server_level)
            || !(target.create(server_level, null, getBlockPos().above(2), MobSpawnType.MOB_SUMMONED, false, false) instanceof LivingEntity new_entity)) {
            return null;
        }
        applyOptionToEntity(new_entity);

        if (server_level.addFreshEntity(new_entity)) {
            return new_entity;
        } else {
            return null;
        }
    }

    /// Called to apply various elements to individual entity
    protected void applyOptionToEntity(LivingEntity entity) {
        if (entity instanceof EnderDragon dragon) {
            dragon.setFightOrigin(getBlockPos().above(2));
        }
        if (getLevel() instanceof ServerLevel server_level) {
            entity.setData(AttachmentRegistry.POSITION, new ExtendedBlockPos(getBlockPos(), server_level));
        }
        entity.addTag("Summoned");
    }

    /// Checks whether it should despawns. `true` if should despawns, otherwise `false`.
    ///
    /// Default implementation: Check if there's no players nearby and call `onEnd()` when `true`
    public boolean checkDespawn() {
        if (getPlayers().isEmpty()) {
            onEnd();
            return true;
        }
        return false;
    }

    /// Called after first wave had spawned
    protected void onStart() {}

    /// Called whenever a wave is called (successful or not)
    protected void onWave() {}

    /// Alternative to onEnd when a wave was tried to be called
    /// but `isAllWavesComplete()` yields true
    ///
    /// Default implementation: Calls `onEnd()`
    protected void onComplete() {
        onEnd();
    }

    /// Called when this challenge had ended due to various
    /// reasons, and such is used for clearing up
    ///
    /// Default implementation: Resets the state of the object
    protected void onEnd() {
        for (LivingEntity entity : active) {
            entity.kill();
        }
        this.active.clear();
        this.wave = 0;
    }

    /// Called by `onTick()` when the list of entities are empty.
    ///
    /// Default implementation: Calls `spawnNextWave()`
    protected void onEmpty() {
        spawnNextWave();
    }

    /// Called after a wave had spawned successfully
    protected void onSpawnSuccess() {}

    /// Called every tick.
    ///
    /// Default implementation: Remove non-alive entities and calls `onEmpty()` when no more are left
    public void onTick() {
        if (!active.isEmpty() && !checkDespawn()) {
            this.active.removeIf(entity -> !entity.isAlive());
            if (this.active.isEmpty()) {
                onEmpty();
            }
        }
    }
}
