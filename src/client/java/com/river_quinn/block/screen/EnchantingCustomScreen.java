package com.river_quinn.block.screen;

import com.river_quinn.EnchantmentCustomTable;
import com.river_quinn.blocks.screen_handler.EnchantingCustomScreenHandler;
import com.river_quinn.network.enchanting_custom_table.EnchantingCustomTableNetData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantingCustomScreen extends HandledScreen<EnchantingCustomScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(EnchantmentCustomTable.MOD_ID, "textures/screens/enchanting_custom.png");

    private final EnchantingCustomScreenHandler handler;
    ButtonWidget button_left_arrow_button;
    ButtonWidget button_right_arrow_button;
    ButtonWidget export_button;

    public EnchantingCustomScreen(EnchantingCustomScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    public String generatePageText() {
        int currentPage = this.handler.currentPage;
        int totalPage = this.handler.totalPage;
        if (totalPage == 0)
            return "-/-";
        return (currentPage + 1) + "/" + totalPage;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
//        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
//        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                generatePageText(),
                35,
                33,
                -1
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight,
                this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void init() {
        super.init();
        button_left_arrow_button = new ButtonWidget.Builder(
                Text.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                button -> {
                    // Handle left arrow button click
                    EnchantingCustomTableNetData payload = new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.PREVIOUS_PAGE.name()
                    );
                    ClientPlayNetworking.send(payload);
                }
        ).dimensions(this.x + 7, this.y + 43, 26, 18).build();
        this.addDrawableChild(button_left_arrow_button);

        button_right_arrow_button = new ButtonWidget.Builder(
                Text.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                button -> {
                    // Handle right arrow button click
                    EnchantingCustomTableNetData payload = new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.NEXT_PAGE.name()
                    );
                    ClientPlayNetworking.send(payload);
                }
        ).dimensions(this.x + 33, this.y + 43, 26, 18).build();
        this.addDrawableChild(button_right_arrow_button);

        export_button = new ButtonWidget.Builder(
                Text.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                button -> {
                    // Handle export button click
                    EnchantingCustomTableNetData payload = new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.EXPORT_ALL_ENCHANTMENTS.name()
                    );
                    ClientPlayNetworking.send(payload);
                }
        ).dimensions(this.x + 7, this.y + 61, 52, 18).build();
        this.addDrawableChild(export_button);
    }
}
