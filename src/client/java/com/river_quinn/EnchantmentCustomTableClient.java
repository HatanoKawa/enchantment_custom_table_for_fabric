package com.river_quinn;

import com.river_quinn.block.screen.EnchantmentConversionScreen;
import com.river_quinn.init.ModBlockEntities;
import com.river_quinn.init.ModScreensHandler;
import com.river_quinn.block.screen.EnchantingCustomScreen;
import com.river_quinn.renderer.EnchantingCustomTableBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class EnchantmentCustomTableClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(ModScreensHandler.ENCHANTING_CUSTOM_SCREEN_HANDLER, EnchantingCustomScreen::new);
		HandledScreens.register(ModScreensHandler.ENCHANTMENT_CONVERSION_SCREEN_HANDLER, EnchantmentConversionScreen::new);

		BlockEntityRendererFactories.register(
				ModBlockEntities.ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY,
				EnchantingCustomTableBlockEntityRenderer::new);

		BlockEntityRendererFactories.register(
				ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY,
				EnchantingCustomTableBlockEntityRenderer::new);

	}
}