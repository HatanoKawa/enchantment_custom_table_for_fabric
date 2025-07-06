package com.river_quinn.network.enchanting_custom_table;

import com.river_quinn.blocks.screen_handler.EnchantingCustomScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class EnchantingCustomTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantingCustomTableNetData payload, final ServerPlayNetworking.Context context) {
        EnchantingCustomScreenHandler containerMenu = (EnchantingCustomScreenHandler)context.player().currentScreenHandler;

        switch (EnchantingCustomTableNetData.OperateType.valueOf(payload.operateType())) {
            case EXPORT_ALL_ENCHANTMENTS -> {
                containerMenu.exportAllEnchantments();
            }
            case NEXT_PAGE -> {
                containerMenu.nextPage();
            }
            case PREVIOUS_PAGE -> {
                containerMenu.previousPage();
            }
        }
    }

}
