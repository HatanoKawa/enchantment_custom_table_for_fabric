package com.river_quinn.blocks.screen_handler;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.init.ModScreensHandler;
import com.river_quinn.utils.EnchantmentUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentConversionScreenHandler extends ScreenHandler {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;

    public static final int MINIMUM_EMERALD_COST = 36;
    public static final int MINIMUM_EMERALD_BLOCK_COST = 4;

    /**
     * index 0: 待附魔工具槽
     * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
     * index 2-22: 附魔书槽
     */
    public final SimpleInventory containerInventory = new SimpleInventory(ENCHANTMENT_CONVERSION_SLOT_SIZE);

    private static final Logger LOGGER = LogUtils.getLogger();

    public final PlayerEntity player;
    public final PlayerInventory playerInventory;
    public final World world;
    public int posX, posY, posZ;
    //    public BlockEntity blockEntity;
    public final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();

    public EnchantmentConversionScreenHandler(int syncId, PlayerInventory inventory) {
        super(ModScreensHandler.ENCHANTMENT_CONVERSION_SCREEN_HANDLER, syncId);

        player = inventory.player;
        playerInventory = inventory;
        world = player.getWorld();
        posX = player.getBlockPos().getX();
        posY = player.getBlockPos().getY();
        posZ = player.getBlockPos().getZ();

        // 注：在 fabric 中，我目前没找到方法获取 BlockEntity 的位置坐标，先搁置
//        blockEntity = world.getBlockEntity(player.getBlockPos());
//        if (blockEntity != null) {
//            posX = blockEntity.getPos().getX();
//            posY = blockEntity.getPos().getY();
//            posZ = blockEntity.getPos().getZ();
//        } else {
//            posX = 0;
//            posY = 0;
//            posZ = 0;
//        }

        this.addSlot(new Slot(containerInventory, 0, 16, 8) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return Items.BOOK == stack.getItem();
            }

            // setByPlayer in neoforge
            @Override
            public void setStack(ItemStack newStack, ItemStack oldStack) {
                super.setStack(newStack, oldStack);
                if (!newStack.isEmpty() && !oldStack.isEmpty()) {
                    genEnchantedBookSlot();
                } else {
                    regenerateEnchantedBookSlot();
                }
            }

            @Override
            public @Nullable Identifier getBackgroundSprite() {
                return Identifier.tryParse("enchantment_custom_table:container/slot/empty_slot_book");
            }
        });

        this.addSlot(new Slot(containerInventory, 1, 16, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return Items.EMERALD == stack.getItem() || Items.EMERALD_BLOCK == stack.getItem();
            }

            // setByPlayer in neoforge
            @Override
            public void setStack(ItemStack newStack, ItemStack oldStack) {
                super.setStack(newStack, oldStack);
                if (!newStack.isEmpty() && !oldStack.isEmpty()) {
                    genEnchantedBookSlot();
                } else {
                    regenerateEnchantedBookSlot();
                }
            }

            @Override
            public @Nullable Identifier getBackgroundSprite() {
                return Identifier.tryParse("minecraft:container/slot/emerald");
            }
        });

        int enchanted_book_index = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = 8 + row * 18;
            for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
                int xPos = 43 + col * 18;
                int final_enchanted_book_index = enchanted_book_index;
                this.enchantedBookSlots.put(final_enchanted_book_index, addSlot(
                    new Slot(containerInventory, final_enchanted_book_index + 2, xPos, yPos) {
                        private final int slot = final_enchanted_book_index + 2;

                        @Override
                        public boolean canInsert(ItemStack stack) {
                            return false;
                        }

                        @Override
                        public @Nullable Identifier getBackgroundSprite() {
                            return Identifier.tryParse("enchantment_custom_table:container/slot/empty_slot_book");
                        }

                        // setByPlayer in neoforge
                        @Override
                        public void setStack(ItemStack newStack, ItemStack oldStack) {
                            super.setStack(newStack, oldStack);
                            pickEnchantedBook();
                        }
                    }
                ));
                enchanted_book_index++;
            }
        }

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 0 + 8 + si * 18, 0 + 142));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = getSlot(slotIndex);
        ItemStack itemStackToOperate = slot.getStack().copy();
        if (slot != null && slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotIndex < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
                if (!this.insertItem(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE, this.slots.size(), true))
                    return ItemStack.EMPTY;
                slot.onQuickTransfer(itemstack1, itemstack);
            } else if (!this.insertItem(itemstack1, 0, ENCHANTMENT_CONVERSION_SLOT_SIZE, false)) {
                if (slotIndex < ENCHANTMENT_CONVERSION_SLOT_SIZE + 27) {
                    if (!this.insertItem(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE + 27, this.slots.size(), true))
                        return ItemStack.EMPTY;
                } else {
                    if (!this.insertItem(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE, ENCHANTMENT_CONVERSION_SLOT_SIZE + 27, false))
                        return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
            if (itemstack1.getCount() == itemstack.getCount())
                return ItemStack.EMPTY;
            slot.onTakeItem(player, itemstack1);
        }


        if (slotIndex < 2) {
            genEnchantedBookSlot();
        } else if (slotIndex < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
            pickEnchantedBook();
        }

        return itemstack;
    }

    @Override
    protected boolean insertItem(ItemStack targetItemStack, int startIndex, int endIndex, boolean fromLast) {
        boolean flag = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        if (targetItemStack.isStackable()) {
            while (!targetItemStack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                Slot slot = getSlot(i);
                ItemStack itemstack = slot.getStack();
                if (slot.canInsert(itemstack) && !itemstack.isEmpty() && ItemStack.areItemsAndComponentsEqual(targetItemStack, itemstack)) {
                    int j = itemstack.getCount() + targetItemStack.getCount();
                    int k = slot.getMaxItemCount(itemstack);
                    if (j <= k) {
                        targetItemStack.setCount(0);
                        itemstack.setCount(j);
                        slot.setStack(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < k) {
                        targetItemStack.decrement(k - itemstack.getCount());
                        itemstack.setCount(k);
                        slot.setStack(itemstack);
                        flag = true;
                    }
                }
                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }
        if (!targetItemStack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }
            while (fromLast ? i >= startIndex : i < endIndex) {
                Slot slot1 = getSlot(i);
                ItemStack itemstack1 = slot1.getStack();
                if (itemstack1.isEmpty() && slot1.canInsert(targetItemStack)) {
                    int l = slot1.getMaxItemCount(targetItemStack);
                    slot1.setStack(targetItemStack.split(Math.min(targetItemStack.getCount(), l)));
                    slot1.markDirty();
                    flag = true;
                    break;
                }
                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }
        return flag;
    }

    // removed in neoforge
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(containerInventory.getStack(0));
            player.getInventory().offerOrDrop(containerInventory.getStack(1));
        }
    }

    public static final List<Integer> allEnchantments = new ArrayList<>();

    public void tryGetAllEnchantments() {
        if (allEnchantments.isEmpty()) {
            Registry<Enchantment> fullEnchantmentList = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            IndexedIterable<RegistryEntry<Enchantment>> allRegisteredEnchantments = fullEnchantmentList.getIndexedEntries();
            allRegisteredEnchantments.forEach(enchantment ->
                    allEnchantments.add(fullEnchantmentList.getRawId(enchantment.value())));
        }
    }

    public ItemStack getEnchantedBook(int enchantmentId) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

        Enchantment enchantment = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).get(enchantmentId);
        int enchantmentLevel = enchantment.getMaxLevel();
        var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);
        assert enchantmentReference != null;
        enchantedBook.addEnchantment(enchantmentReference, enchantmentLevel);

        return enchantedBook;
    }

    public int currentPage = 0;
    public int totalPage = 0;

    public void nextPage() {
        if (currentPage < (totalPage - 1)) {
            turnPage(currentPage + 1);
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            turnPage(currentPage - 1);
        }
    }

    public void turnPage(int page) {
        currentPage = page;
        clearEnchantedBookSlot();
        genEnchantedBookSlot();
    }

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void clearEnchantedBookSlot() {
        for (int i = 2; i < ENCHANTMENT_CONVERSION_SLOT_SIZE; i++) {
            containerInventory.setStack(i, ItemStack.EMPTY);
        }
    }

    public void genEnchantedBookSlot() {
        tryGetAllEnchantments();
        boolean hasBook = containerInventory.getStack(0).isOf(Items.BOOK);
        boolean hasEnoughEmerald = false;
        if (containerInventory.getStack(1).isOf(Items.EMERALD)) {
            hasEnoughEmerald = containerInventory.getStack(1).getCount() >= MINIMUM_EMERALD_COST;
        } else if (containerInventory.getStack(1).isOf(Items.EMERALD_BLOCK)) {
            hasEnoughEmerald = containerInventory.getStack(1).getCount() >= MINIMUM_EMERALD_BLOCK_COST;
        }

        if (!hasBook || !hasEnoughEmerald) {
            resetPage();
            clearEnchantedBookSlot();
            return;
        }

        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            int slotIndex = i + 2;
            int enchantmentIndex = i + currentPage * ENCHANTED_BOOK_SLOT_SIZE;

            if (enchantmentIndex < allEnchantments.size()) {
                if (containerInventory.getStack(slotIndex).isEmpty()) {
                    int enchantmentId = allEnchantments.get(enchantmentIndex);
                    containerInventory.setStack(slotIndex, getEnchantedBook(enchantmentId));
                }
            } else {
                containerInventory.setStack(slotIndex, ItemStack.EMPTY);
            }
        }
    }

    public void regenerateEnchantedBookSlot() {
        currentPage = 0;
        totalPage = (int) Math.ceil(allEnchantments.size() / (double) ENCHANTED_BOOK_SLOT_SIZE);
        genEnchantedBookSlot();
    }

    public void pickEnchantedBook() {
        containerInventory.getStack(0).decrement(1);
        if (containerInventory.getStack(1).isOf(Items.EMERALD))
            containerInventory.getStack(1).decrement(MINIMUM_EMERALD_COST);
        else if (containerInventory.getStack(1).isOf(Items.EMERALD_BLOCK))
            containerInventory.getStack(1).decrement(MINIMUM_EMERALD_BLOCK_COST);
        genEnchantedBookSlot();
    }

}
