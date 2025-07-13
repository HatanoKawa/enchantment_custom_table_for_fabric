package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.EnchantingCustomTableBlock;
import com.river_quinn.blocks.EnchantmentConversionTableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {

    private static Block register(String path, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        final Identifier identifier = Identifier.of(EnchantmentCustomTable.MOD_ID, path);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        final Block block = Blocks.register(registryKey, factory, settings);
        Items.register(block);
        return block;
    }

    public static final Block ENCHANTING_CUSTOM_TABLE = register(
            "enchanting_custom_table",
            EnchantingCustomTableBlock::new,
            AbstractBlock.Settings
                    .create()
                    .sounds(BlockSoundGroup.STONE)
                    .strength(1, 3600)
                    .luminance(state -> 15)
    );

    public static final Block ENCHANTMENT_CONVERSION_TABLE = register(
            "enchantment_conversion_table",
            EnchantmentConversionTableBlock::new,
            AbstractBlock.Settings
                    .create()
                    .sounds(BlockSoundGroup.STONE)
                    .strength(1, 3600)
                    .luminance(state -> 15)
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register((itemGroup) -> {
                    itemGroup.add(ENCHANTING_CUSTOM_TABLE.asItem());
                    itemGroup.add(ENCHANTMENT_CONVERSION_TABLE.asItem());
                });
    }
}
