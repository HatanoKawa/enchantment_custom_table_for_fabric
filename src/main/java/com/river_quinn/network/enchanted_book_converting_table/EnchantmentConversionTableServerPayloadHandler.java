package com.river_quinn.network.enchanted_book_converting_table;

import com.river_quinn.blocks.screen_handler.EnchantmentConversionScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class EnchantmentConversionTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final ServerPlayNetworking.Context context) {
        EnchantmentConversionScreenHandler containerMenu = (EnchantmentConversionScreenHandler)context.player().currentScreenHandler;

        switch (EnchantmentConversionTableNetData.OperateType.valueOf(data.operateType())) {
            case NEXT_PAGE -> {
                containerMenu.nextPage();
            }
            case PREVIOUS_PAGE -> {
                containerMenu.previousPage();
            }
        }
    }
}
