package dev.blankrose.colosseum.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/// Simply yeet the loot table when the conditions are met.
public class VoidLootModifier extends LootModifier {
    /// Mimic ObjectArrayList to prevent any addition of additional items
    /// but still maintain the ability to pass the empty list properly.
    public static class VoidArrayList<T> extends ObjectArrayList<T> {
        @Override
        public void add(int index, T t) {}

        @Override
        public T set(int index, T t) { return null; }
    }

    public static final MapCodec<VoidLootModifier> CODEC = RecordCodecBuilder.mapCodec(
        inst -> LootModifier.codecStart(inst).apply(inst, VoidLootModifier::new)
    );

    protected VoidLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot_table, LootContext context) {
        for (LootItemCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return loot_table;
            }
        }
        return new VoidArrayList<>();
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
