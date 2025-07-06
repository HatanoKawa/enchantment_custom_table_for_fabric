package com.river_quinn.init;

import com.river_quinn.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.network.enchanted_book_converting_table.EnchantmentConversionTableServerPayloadHandler;
import com.river_quinn.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.network.enchanting_custom_table.EnchantingCustomTableServerPayloadHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModPayloads {
    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(
                EnchantingCustomTableNetData.TYPE.id(),
                EnchantingCustomTableNetData.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                EnchantmentConversionTableNetData.TYPE.id(),
                EnchantmentConversionTableNetData.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                EnchantingCustomTableNetData.TYPE.id(),
                EnchantingCustomTableServerPayloadHandler::handleDataOnMain
        );

        ServerPlayNetworking.registerGlobalReceiver(
                EnchantmentConversionTableNetData.TYPE.id(),
                EnchantmentConversionTableServerPayloadHandler::handleDataOnMain
        );
    }
}
