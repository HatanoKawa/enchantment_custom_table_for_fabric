package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.entities.EnchantingCustomTableBlockEntity;
import com.river_quinn.blocks.entities.EnchantmentConversionTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<EnchantingCustomTableBlockEntity> ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY =
        register(
            "enchanting_custom_table",
            FabricBlockEntityTypeBuilder.create(EnchantingCustomTableBlockEntity::new, ModBlocks.ENCHANTING_CUSTOM_TABLE).build()
        );

    public static final BlockEntityType<EnchantmentConversionTableBlockEntity> ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY =
        register(
            "enchantment_conversion_table",
            FabricBlockEntityTypeBuilder.create(EnchantmentConversionTableBlockEntity::new, ModBlocks.ENCHANTMENT_CONVERSION_TABLE).build()
        );

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(EnchantmentCustomTable.MOD_ID, path), blockEntityType);
    }

    public static void initialize() {
    }
}
