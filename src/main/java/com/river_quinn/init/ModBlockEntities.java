package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.entities.EnchantingCustomTableBlockEntity;
import com.river_quinn.blocks.entities.EnchantmentConversionTableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<EnchantingCustomTableBlockEntity> ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY =
            register("enchanting_custom_table", EnchantingCustomTableBlockEntity::new, ModBlocks.ENCHANTING_CUSTOM_TABLE);

    public static final BlockEntityType<EnchantmentConversionTableBlockEntity> ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY =
            register("enchantment_conversion_table", EnchantmentConversionTableBlockEntity::new, ModBlocks.ENCHANTMENT_CONVERSION_TABLE);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       BlockEntityType.BlockEntityFactory<? extends T> entityFactory,
                                                                       Block... blocks) {
        Identifier id = Identifier.of(EnchantmentCustomTable.MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, BlockEntityType.Builder.<T>create(entityFactory, blocks).build());
    }

    public static void initialize() {
    }
}
