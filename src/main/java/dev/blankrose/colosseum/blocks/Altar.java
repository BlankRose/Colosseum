package dev.blankrose.colosseum.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/// Handles the behavior of the Altar block, used
/// for boss summoning.
///
/// Can hold these data:
/// - IsActive: Whether this altar is currently in use
/// - Enemies: List of enemies it keeps track of
/// - Wave: Wave data for multi-wave enemies (such as boss rush)
/// - WaveCount: Tracks which wave is active
public class Altar extends HorizontalDirectionalBlock implements EntityBlock {
    private static final MapCodec<Altar> CODEC = simpleCodec(Altar::new);

    protected Altar(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AltarEntity(blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == Items.AIR
            || !(player instanceof ServerPlayer server_player)
            || !(level instanceof ServerLevel server_level)) {
            return ItemInteractionResult.CONSUME_PARTIAL;
        }
        if (stack.getItem() != Items.NETHER_STAR) {
            server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("colosseum.string.altar.unknown_item")));
            return ItemInteractionResult.CONSUME_PARTIAL;
        }

        server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("colosseum.string.altar.start")));
        EntityType.LIGHTNING_BOLT.spawn(server_level, pos.above(), MobSpawnType.MOB_SUMMONED);
        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 100.0f, 1.0f);

        if (!server_player.isCreative()) {
            stack.setCount(stack.getCount() - 1);
        }

        return ItemInteractionResult.SUCCESS;
    }
}
