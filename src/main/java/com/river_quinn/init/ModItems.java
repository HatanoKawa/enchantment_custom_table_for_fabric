package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(EnchantmentCustomTable.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

//    public static final Item ENCHANTING_CUSTOM_TABLE = register(
//        new Item(new Item.Settings()),
//        "enchanting_custom_table"
//    );
//
//    public static final Item ENCHANTMENT_CONVERSION_TABLE = register(
//        new Item(new Item.Settings()),
//        "enchantment_conversion_table"
//    );

    public static void initialize() {
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
//            .register((itemGroup) -> {
//                itemGroup.add(ModItems.ENCHANTING_CUSTOM_TABLE);
//                itemGroup.add(ModItems.ENCHANTMENT_CONVERSION_TABLE);
//            });
    }
}
