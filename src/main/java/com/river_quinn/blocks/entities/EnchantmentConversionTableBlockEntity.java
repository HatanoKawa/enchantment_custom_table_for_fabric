package com.river_quinn.blocks.entities;

import com.river_quinn.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity {
    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY, pos, state);
    }
}
