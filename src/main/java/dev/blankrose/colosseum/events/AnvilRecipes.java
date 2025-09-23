package dev.blankrose.colosseum.events;

import dev.blankrose.colosseum.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.Objects;

public class AnvilRecipes {
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        final ItemStack left = event.getLeft();
        final ItemStack right = event.getRight();

        if (left.getMaxDamage() > 0 && Objects.isNull(left.get(DataComponents.UNBREAKABLE))
            && right.getItem().equals(ItemRegistry.ETERNITY_SHARD.get())) {

            final ItemStack output = left.copy();
            output.setCount(1);
            output.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            output.setDamageValue(output.getMaxDamage());

            event.setCost(50);
            event.setMaterialCost(1);
            event.setOutput(output);
        }
    }
}
