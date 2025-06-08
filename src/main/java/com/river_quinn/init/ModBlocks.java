package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.EnchantingCustomTableBlock;
import com.river_quinn.blocks.EnchantmentConversionTableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        Identifier id = Identifier.of(EnchantmentCustomTable.MOD_ID, name);

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static final Block ENCHANTING_CUSTOM_TABLE = register(
            new EnchantingCustomTableBlock(
                AbstractBlock.Settings
                    .create()
                    .sounds(BlockSoundGroup.STONE)
                    .strength(1, 3600)
                    .luminance(state -> 15)),
            "enchanting_custom_table",
            true
    );

    public static final Block ENCHANTMENT_CONVERSION_TABLE = register(
            new EnchantmentConversionTableBlock(
                AbstractBlock.Settings
                    .create()
                    .sounds(BlockSoundGroup.STONE)
                    .strength(1, 3600)
                    .luminance(state -> 15)),
            "enchantment_conversion_table",
            true
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register((itemGroup) -> {
                    itemGroup.add(ENCHANTING_CUSTOM_TABLE.asItem());
                    itemGroup.add(ENCHANTMENT_CONVERSION_TABLE.asItem());
                });
    }
}
