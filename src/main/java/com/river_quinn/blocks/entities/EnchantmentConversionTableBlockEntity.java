package com.river_quinn.blocks.entities;

import com.river_quinn.init.ModBlockEntities;
import com.river_quinn.init.ModScreensHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity implements NamedScreenHandlerFactory {
    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.of("");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return ModScreensHandler.ENCHANTMENT_CONVERSION_SCREEN_HANDLER.create(syncId, playerInventory);
    }
}
