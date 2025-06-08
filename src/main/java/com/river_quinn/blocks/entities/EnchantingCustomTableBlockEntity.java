package com.river_quinn.blocks.entities;

import com.river_quinn.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EnchantingCustomTableBlockEntity extends EnchantingTableLikeBlockEntity {
    public EnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY, pos, state);
    }
}
