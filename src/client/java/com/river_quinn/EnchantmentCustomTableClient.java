package com.river_quinn;

import com.river_quinn.init.ModBlockEntities;
import com.river_quinn.renderer.EnchantingCustomTableBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;

public class EnchantmentCustomTableClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(
				ModBlockEntities.ENCHANTING_CUSTOM_TABLE_BLOCK_ENTITY,
				EnchantingCustomTableBlockEntityRenderer::new);

		BlockEntityRendererFactories.register(
				ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE_BLOCK_ENTITY,
				EnchantingCustomTableBlockEntityRenderer::new);
	}
}