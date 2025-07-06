package com.river_quinn.blocks;

import com.river_quinn.blocks.entities.EnchantingCustomTableBlockEntity;
import com.river_quinn.init.ModBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantingCustomTableBlock extends EnchantingTableLikeBlock {
    public EnchantingCustomTableBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantingCustomTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? validateTicker(type, ModBlockEntities.ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY, EnchantingCustomTableBlockEntity::tick) : null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingCustomTableBlockEntity enchantingCustomTable) {
            player.openHandledScreen(enchantingCustomTable);
            return ActionResult.CONSUME;
        } else {
            return ActionResult.PASS;
        }
    }
}
