package dev.blankrose.colosseum.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExtendedBlockPos {
    protected BlockPos pos;
    protected ResourceLocation dim;

    public ExtendedBlockPos() {
        this.pos = new BlockPos(0, 0, 0);
        this.dim = null;
    }

    public ExtendedBlockPos(BlockPos pos, ResourceLocation dimension) {
        this.pos = pos;
        this.dim = dimension;
    }

    public ExtendedBlockPos(BlockPos pos, Level level) {
        this.pos = pos;
        this.dim = level.dimension().location();
    }

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public ResourceLocation getDim() {
        return dim;
    }

    @Nullable
    public ServerLevel getLevel(MinecraftServer server) {
        if (Objects.isNull(dim)) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().equals(dim)) {
                return level;
            }
        }
        return null;
    }

    public static class Serializer implements IAttachmentSerializer<CompoundTag, ExtendedBlockPos> {
        @Override
        public ExtendedBlockPos read(IAttachmentHolder iAttachmentHolder, CompoundTag tag, HolderLookup.Provider provider) {
            final BlockPos pos = new BlockPos(tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
            if (tag.contains("dim")) {
                final ResourceLocation dim = ResourceLocation.tryParse(tag.getString("dim"));
                return new ExtendedBlockPos(pos, dim);
            } else {
                return new ExtendedBlockPos(pos, (ResourceLocation) null);
            }
        }

        @Override
        public @Nullable CompoundTag write(ExtendedBlockPos position, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", position.getPos().getX());
            tag.putInt("y", position.getPos().getY());
            tag.putInt("z", position.getPos().getZ());
            if (Objects.nonNull(position.getDim())) {
                tag.putString("dim", position.getDim().toString());
            }
            return tag;
        }
    }
}
