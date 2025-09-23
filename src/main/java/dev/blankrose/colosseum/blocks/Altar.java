package dev.blankrose.colosseum.blocks;

import com.mojang.serialization.MapCodec;
import dev.blankrose.colosseum.sounds.MusicPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/// Handles the behavior of the Altar block,
/// used for boss summoning.
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
        return super.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection())
            .setValue(BlockStateProperties.ENABLED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.ENABLED);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityRegistry.ALTAR.get() ? AltarEntity::tick : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == Items.AIR
            || !(player instanceof ServerPlayer server_player)
            || !(level instanceof ServerLevel server_level)
            || !(level.getBlockEntity(pos) instanceof AltarEntity block_entity)) {
            return ItemInteractionResult.CONSUME_PARTIAL;
        }
        if (block_entity.handler.isActive()) {
            server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("block.colosseum.altar.in_use")));
            return ItemInteractionResult.CONSUME_PARTIAL;
        }
        if (stack.getItem() != Items.NETHER_STAR) {
            server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("block.colosseum.altar.unknown_item")));
            return ItemInteractionResult.CONSUME_PARTIAL;
        }

        if (!server_player.isCreative()) {
            stack.setCount(stack.getCount() - 1);
        }

        server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("block.colosseum.altar.start")));
        block_entity.handler.spawnNextWave();
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.getValue(BlockStateProperties.ENABLED)) {
            if (level.isClientSide()) {
                if (AltarEntity.IsNear(level, pos)) {
                    MusicPlayer.stop();
                }
            } else /* isServerSide */ {
                BlockEntity block_entity = level.getBlockEntity(pos);
                if (block_entity instanceof AltarEntity altar_entity) {
                    altar_entity.handler.finish();
                }
                if (player instanceof ServerPlayer server_player) {
                    server_player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("block.colosseum.altar.break")));
                }
            }
            return true;
        } else {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
    }
}
