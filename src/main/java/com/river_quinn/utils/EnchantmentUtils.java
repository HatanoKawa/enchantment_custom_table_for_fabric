package com.river_quinn.utils;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

import java.util.Optional;

public class EnchantmentUtils {
    public static ComponentType<ItemEnchantmentsComponent> getEnchantmentsComponentType(ItemStack stack) {
        return stack.isOf(Items.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    public static RegistryEntry.Reference<Enchantment> translateEnchantment(World world, Enchantment enchantment) {
        if (world == null)
            return null;
        Registry<Enchantment> fullEnchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryKey<Enchantment> resourceKey = fullEnchantmentRegistry.getKey(enchantment).get();
        // 一些通过猜谜获得的逻辑，我不知道为什么要这么做，但是这么做能行
        Optional<RegistryEntry.Reference<Enchantment>> optional = world
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                .getOptional(resourceKey);
        return optional.get();
    }

    // 这功能不想做了，没啥意义
//    public static int getEnchantCost(ItemStack toolItemStack) {
//        var xpLevelToCost = 0;
//        var itemEnchantments = toolItemStack.get(EnchantmentHelper.getComponentType(toolItemStack));
//        for (var entry : itemEnchantments.entrySet()) {
//            var enchantment = entry.getKey();
//            var level = entry.getValue();
//
//            xpLevelToCost += enchantment.value().getAnvilCost() * level;
//        }
//        return xpLevelToCost;
//    }

//    public static boolean checkSatisfyXpRequirement(ItemStack toolItemStack, Player player) {
//        var xpLevelToCost = getEnchantCost(toolItemStack);
//        return xpLevelToCost <= player.experienceLevel;
//    }
}
