package dev.blankrose.colosseum.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class BossRush extends AbstractWaveChallenge {
    public BossRush(Level world, BlockPos pos) {
        super(world, pos);
    }

    protected HolderSet.Named<EntityType<?>> getBossList() {
        return BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(Tags.EntityTypes.BOSSES);
    }

    @Override
    public @Nullable Collection<EntityType<?>> getWave(int index) {
        if (index < 0) { return null; }

        HolderSet.Named<EntityType<?>> list = getBossList();
        if (list.size() <= index) { return null; }

        return List.of(list.get(index).value());
    }

    @Override
    public boolean isAllWavesComplete() {
        return getCurrentWave() > getBossList().size();
    }
}
