package dev.blankrose.colosseum.blocks;

import dev.blankrose.colosseum.Colosseum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class AltarEntity extends BlockEntity {
    public AltarEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ALTAR_BLOCK_ENTITY.get(), pos, blockState);
        this.wave = 0;
        this.boss = null;
    }

    private static final String[] RAW_BOSS_LIST = {
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

    // --- STORAGE --- //

    /// Index of current wave
    public int wave;
    /// Currently alive enemies
    public Entity boss;

    // --- SAVE & LOAD --- //

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("wave", this.wave);
        if (Objects.nonNull(boss)) {
            tag.putUUID("boss", this.boss.getUUID());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.wave = tag.getInt("wave");
        final UUID uuid = tag.getUUID("boss");
        final Level level = super.getLevel();
        if (Objects.nonNull(level)) {
            this.boss = level.getEntities((Entity) null, AABB.INFINITE, entity -> entity.getUUID() == uuid).getFirst();
        }
    }
}
