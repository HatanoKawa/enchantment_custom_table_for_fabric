package com.river_quinn.blocks;

import com.mojang.serialization.MapCodec;
import com.river_quinn.blocks.entities.EnchantingTableLikeBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableLikeBlock extends BlockWithEntity {
    public static final MapCodec<EnchantingTableLikeBlock> CODEC = createCodec(EnchantingTableLikeBlock::new);
    protected static final VoxelShape SHAPE = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)12.0F, (double)16.0F);
//    public static final List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos.stream(-2, 0, -2, 2, 1, 2).filter((pos) -> Math.abs(pos.getX()) == 2 || Math.abs(pos.getZ()) == 2).map(BlockPos::toImmutable).toList();

    public MapCodec<EnchantingTableLikeBlock> getCodec() {
        return CODEC;
    }

    public EnchantingTableLikeBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

//    public static boolean canAccessPowerProvider(World world, BlockPos tablePos, BlockPos providerOffset) {
//        return world.getBlockState(tablePos.add(providerOffset)).isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER) && world.getBlockState(tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
//    }

    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

//    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
//        super.randomDisplayTick(state, world, pos, random);
//
//        for(BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
//            if (random.nextInt(16) == 0 && canAccessPowerProvider(world, pos, blockPos)) {
//                world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)2.0F, (double)pos.getZ() + (double)0.5F, (double)((float)blockPos.getX() + random.nextFloat()) - (double)0.5F, (double)((float)blockPos.getY() - random.nextFloat() - 1.0F), (double)((float)blockPos.getZ() + random.nextFloat()) - (double)0.5F);
//            }
//        }
//
//    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

//    @Nullable
//    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
//        return world.isClient ? validateTicker(type, BlockEntityType.ENCHANTING_TABLE, EnchantingTableBlockEntity::tick) : null;
//    }

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingTableLikeBlockEntity) {
            Text text = ((Nameable)blockEntity).getDisplayName();
            return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), text);
        } else {
            return null;
        }
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}
