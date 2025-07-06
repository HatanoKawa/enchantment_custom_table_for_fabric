package com.river_quinn.init;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.screen_handler.EnchantingCustomScreenHandler;
import com.river_quinn.blocks.screen_handler.EnchantmentConversionScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreensHandler {
    public static final ScreenHandlerType<EnchantingCustomScreenHandler> ENCHANTING_CUSTOM_SCREEN_HANDLER =
            register("enchanting_custom", EnchantingCustomScreenHandler::new);

    public static final ScreenHandlerType<EnchantmentConversionScreenHandler> ENCHANTMENT_CONVERSION_SCREEN_HANDLER =
            register("enchantment_conversion", EnchantmentConversionScreenHandler::new);

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType.Factory<T> factory) {
        Identifier id = Identifier.of(EnchantmentCustomTable.MOD_ID, name);
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    public static void initialize() {
    }
}
