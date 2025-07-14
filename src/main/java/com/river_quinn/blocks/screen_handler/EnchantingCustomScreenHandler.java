package com.river_quinn.blocks.screen_handler;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.init.ModScreensHandler;
import com.river_quinn.utils.EnchantmentUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantingCustomScreenHandler extends ScreenHandler {
    public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
    public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 6;
    public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
    public static final int ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;

    /**
     * index 0: 待附魔工具槽
     * index 1: 附加槽，仅接受附魔，添加附魔书后将会立刻将附魔书的附魔添加到待附魔工具中并重新生成附魔书槽
     * index 2-22: 附魔书槽
     */
    public final SimpleInventory containerInventory = new SimpleInventory(ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE);

    private static final Logger LOGGER = LogUtils.getLogger();

    public final PlayerEntity player;
    public final PlayerInventory playerInventory;
    public final World world;
    public int posX, posY, posZ;
//    public BlockEntity blockEntity;
    public final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
//        super.onSlotClick(slotIndex, button, actionType, player);

        // 在 1.21.2 版本及以上时，在尝试堆叠 isSameItemSameComponents 判定为 true 的附魔书时不会触发 setByPlayer 方法，
        // 因此将对于附魔书槽操作的逻辑迁移到更底层的 clicked 方法中

        // 仅额外处理部分情况，需要满足以下条件：
        // 1. 点击的槽位的下标在 2-22 之间
        // 2. 点击类型不是快速移动
        // 3. 附魔书槽对应的物品可以放置在该槽位上（主要是待附魔物品槽不能为空）
        var itemStackToPut = this.getCursorStack();
        if (
                slotIndex >= 2 &&
                        slotIndex < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE &&
                        actionType != SlotActionType.QUICK_MOVE &&
                        (itemStackToPut.isEmpty() || getSlot(slotIndex).canInsert(this.getCursorStack()))
        ) {
            var itemStackToReplace = containerInventory.getStack(slotIndex);
            if (!itemStackToPut.isEmpty() && !itemStackToReplace.isEmpty()) {
                // 当尝试替换附魔书槽的附魔书时，存在以下两种情况：
                // 1. 新旧附魔书没有重复的附魔，此时去除工具上的旧附魔，添加新的附魔，返回旧的附魔书
                // 2. 新旧附魔书有重复的附魔，此时直接添加新的附魔书的附魔到工具上，不返回附魔书
                // 此段逻辑用于处理第二种情况

                // 新的物品槽对应的附魔书可能同时有多种附魔
                var enchantmentsOnNewStack = getEnchantmentInstanceFromEnchantedBook(itemStackToPut);
                // 旧的物品槽对应的附魔书最多只有一种附魔
                var enchantmentOnOldStack = getEnchantmentInstanceFromEnchantedBook(itemStackToReplace).get(0);
                var hasDuplicateEnchantment = enchantmentsOnNewStack.stream().anyMatch(enchantment ->
                        enchantment.enchantment.equals(enchantmentOnOldStack.enchantment));
                if (hasDuplicateEnchantment) {
                    // 如果新旧物品槽的对应的附魔书有重复的附魔，则直接添加到工具上，合并附魔并不返回旧的附魔书
                    addEnchantment(itemStackToPut, slotIndex, true);
                    this.setCursorStack(ItemStack.EMPTY.copy());
                    return;
                }
            }

            int enchantmentIndexInCache = (slotIndex - 2) + currentPage * ENCHANTED_BOOK_SLOT_SIZE;

            // 以下逻辑用于处理第一种情况
            if (!itemStackToReplace.isEmpty()) {
                this.setCursorStack(itemStackToReplace.copy());
                // 移除旧的槽位对应附魔书的附魔
                var hasRegenerated = removeEnchantment(itemStackToReplace);
                // 在缓存中删除对应的附魔书
                // 如果移除附魔书导致了总页数变更，将会触发重新生成附魔书缓存，此时对应的附魔书槽下标可能会产生溢出，所以需要进行判断
                if (!hasRegenerated) {
                    enchantmentsOnCurrentTool.set(enchantmentIndexInCache, ItemStack.EMPTY);
                }
            } else {
                // 如果没有待移除的附魔书，则将指针上的物品设置为 0
                this.setCursorStack(ItemStack.EMPTY.copy());
            }
            if (!itemStackToPut.isEmpty()) {
                // 添加新的槽位对应附魔书的附魔
                addEnchantment(itemStackToPut, slotIndex);
            }
            updateEnchantedBookSlots();
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
    }

    public EnchantingCustomScreenHandler(int syncId, PlayerInventory inventory) {
        super(ModScreensHandler.ENCHANTING_CUSTOM_SCREEN_HANDLER, syncId);

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

//        addSlot(new Slot(containerInventory, 0, 8, 8) {
//            // mayPlace in neoforge
//            @Override
//            public boolean canInsert(ItemStack stack) {
//                return super.canInsert(stack);
//            }
//
//            // onQuickCraft in neoforge
//            @Override
//            public void onQuickTransfer(ItemStack newStack, ItemStack oldStack) {
//                super.onQuickTransfer(newItem, original);
//            }
//
//            // setByPlayer in neoforge
//            @Override
//            public void setStack(ItemStack newStack, ItemStack oldStack) {
//                super.setStack(stack);
//            }
//
//            // getNoItemIcon in neoforge
//            @Override
//            public @Nullable Pair<Identifier, Identifier> getBackgroundSprite() {
//                return super.getBackgroundSprite();
//            }
//        });

        this.addSlot(new Slot(containerInventory, 0, 8, 8) {
            // onQuickCraft in neoforge
            @Override
            public void onQuickTransfer(ItemStack newStack, ItemStack oldStack) {
                super.onQuickTransfer(newStack, oldStack);
                clearCache();
                clearPage();
            }

            // setByPlayer in neoforge
            @Override
            public void setStack(ItemStack newStack, ItemStack oldStack) {
                super.setStack(newStack, oldStack);
                if (!newStack.isEmpty()) {
                    // 放置待附魔工具，重新生成附魔书槽
                    genEnchantedBookCache();
                    currentPage = 0;
                    updateEnchantedBookSlots();
                } else {
                    // 取出待附魔工具，清空附魔书槽
                    clearCache();
                    clearPage();
                }
            }
        });

        this.addSlot(new Slot(containerInventory, 1, 42, 8) {
            private final int slot = 1;
            @Override
            public boolean canInsert(ItemStack stack) {
                return Items.ENCHANTED_BOOK == stack.getItem()
                        && !containerInventory.getStack(0).isEmpty();
            }

            @Override
            public @Nullable Identifier getBackgroundSprite() {
                return Identifier.tryParse("enchantment_custom_table:container/slot/empty_slot_book");
            }

            @Override
            public void setStack(ItemStack newStack, ItemStack oldStack) {
                super.setStack(newStack, oldStack);
                if (!newStack.isEmpty()) {
                    // 放置附魔书，同步添加工具上的附魔，并删除附加槽的附魔书，重新生成附魔书槽
                    addEnchantment(newStack, slot, true);
                } else {
                    // 合法情况下不应该存在这种状况
                    LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
                }
            }
        });

        int enchanted_book_index = 0;
        for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
            int yPos = 8 + row * 18;
            for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
                int xPos = 61 + col * 18;
                int final_enchanted_book_index = enchanted_book_index;
                this.enchantedBookSlots.put(final_enchanted_book_index, addSlot(
                        new Slot(containerInventory, final_enchanted_book_index + 2, xPos, yPos) {
                            private final int slot = final_enchanted_book_index + 2;

                            @Override
                            public boolean canInsert(ItemStack stack) {
                                return Items.ENCHANTED_BOOK == stack.getItem()
                                        && !containerInventory.getStack(0).isEmpty();
                            }

                            @Override
                            public @Nullable Identifier getBackgroundSprite() {
                                return Identifier.tryParse("enchantment_custom_table:container/slot/empty_slot_book");
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
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = getSlot(slotIndex);
        ItemStack itemStackToOperate = slot.getStack().copy();
        if (slot != null && slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotIndex < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
                if (!this.insertItem(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, this.slots.size(), true))
                    return ItemStack.EMPTY;
                slot.onQuickTransfer(itemstack1, itemstack);
            } else if (!this.insertItem(itemstack1, 0, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, false)) {
                if (slotIndex < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27) {
                    if (!this.insertItem(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, this.slots.size(), true))
                        return ItemStack.EMPTY;
                } else {
                    if (!this.insertItem(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, false))
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

        if (slotIndex > 1 && slotIndex < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
            removeEnchantment(itemStackToOperate);
        }
        return itemstack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
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
        }
    }


    public List<EnchantmentLevelEntry> getEnchantmentInstanceFromEnchantedBook(ItemStack enchantedBookItemStack) {

        ComponentType<ItemEnchantmentsComponent> componentType = EnchantmentUtils.getEnchantmentsComponentType(enchantedBookItemStack);
        var componentMap = enchantedBookItemStack.getComponents().get(componentType);

        List<EnchantmentLevelEntry> enchantmentOfBook = new ArrayList<>();
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : componentMap.getEnchantmentEntries()) {
            Enchantment enchantment = entry.getKey().value();
            int enchantmentLevel = entry.getIntValue();
            enchantmentOfBook.add(new EnchantmentLevelEntry(RegistryEntry.of(enchantment), enchantmentLevel));
        }

        return enchantmentOfBook;
    }

    // 此方法用于检查附魔等级是否超过最大等级，fabric版本中先禁用这个功能
    public boolean checkCanPlaceEnchantedBook(ItemStack stack) {
        var itemEnchantments = stack.get(EnchantmentUtils.getEnchantmentsComponentType(stack));
        var itemToEnchant = containerInventory.getStack(0);
        var itemEnchantmentsOnTool = itemToEnchant.get(EnchantmentUtils.getEnchantmentsComponentType(itemToEnchant));
        if (itemEnchantmentsOnTool == null) {
            // 待附魔物品槽中没有附魔
            return true;
        }
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments.getEnchantmentEntries()) {
            Enchantment enchantment = entry.getKey().value();
            var enchantmentLevel = entry.getIntValue();
            var enchantmentLevelOnTool = itemEnchantmentsOnTool.getLevel(entry.getKey());
            var maxLevel = enchantment.getMaxLevel();
            if (enchantmentLevelOnTool + enchantmentLevel > maxLevel) {
                // 附魔等级超过最大等级
                return false;
            }
        }
        return true;
    }

    public int currentPage = 0;
    public int totalPage = 0;

    // 存储当前工具槽中的附魔，用于进行翻页操作
    // 列表的长度为 ENCHANTED_BOOK_SLOT_SIZE 的整数倍，对于空物品的长度为 ENCHANTED_BOOK_SLOT_SIZE
    private final List<ItemStack> enchantmentsOnCurrentTool = new ArrayList<>();

    public void exportAllEnchantments() {
        ItemStack toolItemStack = containerInventory.getStack(0);
        ItemEnchantmentsComponent itemEnchantments = toolItemStack.get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack));
        if (toolItemStack.getItem() == Items.ENCHANTED_BOOK) {
            // 如果待附魔物品槽中的物品是附魔书，则直接返回
            playerInventory.offerOrDrop(toolItemStack);
            containerInventory.setStack(0, ItemStack.EMPTY);

            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else if (!toolItemStack.isEmpty() && itemEnchantments != null && !itemEnchantments.isEmpty()) {
            ItemEnchantmentsComponent.Builder mutable = new ItemEnchantmentsComponent.Builder(itemEnchantments);
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments.getEnchantmentEntries()) {
                Enchantment enchantment = entry.getKey().value();
                int enchantmentLevel = entry.getIntValue();

                var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);

                assert enchantmentReference != null;
                // set 方法在 level 小于等于 0 时会移除对应附魔
                mutable.set(enchantmentReference, 0);
                enchantedBook.addEnchantment(enchantmentReference, enchantmentLevel);
            }

            toolItemStack.set(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack), mutable.build());
            playerInventory.offerOrDrop(enchantedBook);

            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        clearCache();
        clearPage();
    }

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

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

    // 保存当前页的附魔，设置新页面并更新附魔书槽
    public void turnPage(int targetPage) {
        if (targetPage < 0 || targetPage >= totalPage) {
            return;
        }
        int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
        for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
            int indexOfFullList = i + indexOffset;
            int indexOfSlot = i + 2;
            if (indexOfFullList < enchantmentsOnCurrentTool.size())
                enchantmentsOnCurrentTool.set(indexOfFullList, containerInventory.getStack(indexOfSlot));
        }
        currentPage = targetPage;
        updateEnchantedBookSlots();
    }


    public void updateEnchantedBookSlots() {
        containerInventory.setStack(1, ItemStack.EMPTY.copy());

        int indexOffset = currentPage * ENCHANTED_BOOK_SLOT_SIZE;
        if (totalPage > 0) {
            // 将附魔书添加到附魔书槽
            for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
                int indexOfFullList = i + indexOffset;
                int indexOfSlot = i + 2;
                containerInventory.setStack(indexOfSlot, enchantmentsOnCurrentTool.get(indexOfFullList));
            }
        }
    }

    public void clearCache() {
        for (int i = 2; i < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE; i++) {
            containerInventory.setStack(i, ItemStack.EMPTY);
        }
        genEnchantedBookCache();
    }

    public void clearPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void genEnchantedBookCache() {
        ItemStack toolItemStack = containerInventory.getStack(0);

        int currentTotalPage = 1;
        enchantmentsOnCurrentTool.clear();

        if (!toolItemStack.isEmpty()) {
            // 若待附魔物品槽不为空，则至少生成一页的附魔书槽
            ItemEnchantmentsComponent enchantments = toolItemStack.get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack));
            currentTotalPage = Math.max((int) Math.ceil((double) enchantments.getEnchantmentEntries().size() / ENCHANTED_BOOK_SLOT_SIZE), 1);

            if (toolItemStack.isOf(Items.ENCHANTED_BOOK) && enchantments.getEnchantmentEntries().size() == 1) {
                // 获取唯一附魔的附魔等级
                var enchantmentObj = enchantments.getEnchantmentEntries().iterator().next();
                var enchantment = enchantmentObj.getKey().value();
                var enchantmentLevel = enchantmentObj.getIntValue();
                // 如果附魔书上的唯一附魔等级大于 1，则需要拆分附魔等级
                // 如果附魔书上的唯一附魔等级等于 1，则不生成附魔书槽
                if (enchantmentLevel > 1) {
                    // 二分法拆分附魔等级
                    var enchantmentLevelList = new ArrayList<Integer>();
                    while (enchantmentLevel > 0) {
                        if (enchantmentLevel == 2) {
                            enchantmentLevelList.add(1);
                            enchantmentLevel = 0;
                        } else {
                            int levelToAdd = (int) Math.ceil((double) enchantmentLevel / 2);
                            enchantmentLevelList.add(levelToAdd);
                            enchantmentLevel -= levelToAdd;
                        }
                    }

                    for (Integer level : enchantmentLevelList) {
                        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                        var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);
                        assert enchantmentReference != null;
                        enchantedBook.addEnchantment(enchantmentReference, level);
                        enchantmentsOnCurrentTool.add(enchantedBook);
                    }
                }
            } else if (!toolItemStack.isOf(Items.ENCHANTED_BOOK) || enchantments.getEnchantmentEntries().size() > 1) {
                // 若待附魔工具槽中的物品不是附魔书，或者附魔词条数量大于 1，那么继续生成附魔书槽
                // 根据待附魔工具槽中的附魔生成对应的附魔书，并添加到 fullEnchantmentList
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
                    Enchantment enchantment = entry.getKey().value();
                    Integer enchantmentLevel = entry.getValue();
                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                    var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);
                    assert enchantmentReference != null;
                    enchantedBook.addEnchantment(enchantmentReference, enchantmentLevel);
                    enchantmentsOnCurrentTool.add(enchantedBook);
                }
            }
        } else {
            // 仅当待附魔工具槽中没有物品时，将总页数设置为 0
            currentTotalPage = 0;
        }
        int totalSlots = currentTotalPage * ENCHANTED_BOOK_SLOT_SIZE;
        // 补全空附魔书槽
        while(enchantmentsOnCurrentTool.size() < totalSlots) {
            enchantmentsOnCurrentTool.add(ItemStack.EMPTY);
        }

        totalPage = currentTotalPage;
    }

    // 获取所有已注册的附魔
    public IndexedIterable<RegistryEntry<Enchantment>> getAllRegisteredEnchantments() {
        Registry<Enchantment> fullEnchantmentList = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        return fullEnchantmentList.getIndexedEntries();
    }

    public void addEnchantment(ItemStack itemStack, int slotIndex) {
        addEnchantment(itemStack, slotIndex, false);
    }

    public void addEnchantment(ItemStack itemStackToPut, int slotIndex, boolean forceRegenerateEnchantedBookStore) {
        var enchantmentInstances = getEnchantmentInstanceFromEnchantedBook(itemStackToPut);
        boolean regenerateEnchantedBookStore = false;

        ItemStack toolItemStack = containerInventory.getStack(0);
        ItemEnchantmentsComponent enchantmentsOnTool = toolItemStack.get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack));
        int sourceEnchantmentCount = enchantmentsOnTool.getEnchantmentEntries().size();

        IndexedIterable<RegistryEntry<Enchantment>> allRegisteredEnchantments = getAllRegisteredEnchantments();
        HashMap<Integer, EnchantmentLevelEntry> resultEnchantmentMap = new HashMap<>();

        // region 遍历待附魔物品槽中物品的附魔
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantmentsOnTool.getEnchantmentEntries()) {
            Enchantment enchantment = entry.getKey().value();
            int enchantmentId = allRegisteredEnchantments.getRawId(RegistryEntry.of(enchantment));
            int enchantmentLevel = entry.getIntValue();

            EnchantmentLevelEntry enchantmentInstance = new EnchantmentLevelEntry(RegistryEntry.of(enchantment), enchantmentLevel);
            resultEnchantmentMap.put(enchantmentId, enchantmentInstance);
        }

        // endregion

        //region 遍历放入的附魔书的附魔

        for (EnchantmentLevelEntry enchantmentInstance : enchantmentInstances) {
            int enchantmentId = allRegisteredEnchantments.getRawId(enchantmentInstance.enchantment);
            if (resultEnchantmentMap.containsKey(enchantmentId)) {
                regenerateEnchantedBookStore = true;
                // 若附魔已经存在，直接相加两者的附魔等级
                resultEnchantmentMap.put(enchantmentId, new EnchantmentLevelEntry(
                        enchantmentInstance.enchantment,
                        resultEnchantmentMap.get(enchantmentId).level + enchantmentInstance.level
                ));
            } else {
                // 若附魔不存在，直接生成同样附魔等级的附魔
                resultEnchantmentMap.put(enchantmentId, new EnchantmentLevelEntry(enchantmentInstance.enchantment, enchantmentInstance.level));
            }
        }

        int resultEnchantmentsCount = resultEnchantmentMap.size();
        //endregion

        //region 将附魔应用到待附魔物品槽中的物品
        ItemEnchantmentsComponent itemEnchantments = toolItemStack.get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack));
        // 转换成可变形式
        ItemEnchantmentsComponent.Builder mutable = new ItemEnchantmentsComponent.Builder(itemEnchantments);
        for (EnchantmentLevelEntry enchantmentInstance : resultEnchantmentMap.values().stream().toList()) {
            var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantmentInstance.enchantment.value());
            assert enchantmentReference != null;
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(enchantmentReference, enchantmentInstance.level);
        }
        toolItemStack.set(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack), mutable.build());
        // endregion

        // 新增附魔，重新生成所有附魔书缓存并更新附魔书槽
        genEnchantedBookCache();
        updateEnchantedBookSlots();

        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public boolean removeEnchantment(ItemStack itemStackToRemove) {
        var enchantmentInstances = getEnchantmentInstanceFromEnchantedBook(itemStackToRemove);

        //region 将附魔应用到待附魔物品槽中的物品
        ItemStack toolItemStack = containerInventory.getStack(0);
        ItemEnchantmentsComponent itemEnchantments = toolItemStack.get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack));
        // 转换成可变形式
        ItemEnchantmentsComponent.Builder mutable = new ItemEnchantmentsComponent.Builder(itemEnchantments);
        for (EnchantmentLevelEntry enchantmentInstance : enchantmentInstances) {
            var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantmentInstance.enchantment.value());
            var enchantmentLevelSource = containerInventory.getStack(0)
                    .get(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack))
                    .getEnchantmentEntries().stream()
                    .filter(entry -> enchantmentReference.getKey().get().getRegistryRef()
                            == entry.getKey().getKey().get().getRegistryRef())
                    .findFirst().get().getIntValue();
            var enchantmentLevelToMinus = enchantmentInstance.level;

            assert enchantmentReference != null;
            // set 方法在 level 小于等于 0 时会移除对应附魔
            mutable.set(enchantmentReference, enchantmentLevelSource - enchantmentLevelToMinus);
        }
        toolItemStack.set(EnchantmentUtils.getEnchantmentsComponentType(toolItemStack), mutable.build());
        // endregion

        int resultPageSize = Math.max((int) Math.ceil((double) mutable.getEnchantments().size() / ENCHANTED_BOOK_SLOT_SIZE), 1);
        // 在以下情况重新生成附魔书槽：
        // 1. 待附魔物品本身是附魔书，并且附魔后的附魔词条数量为 1
        // 2. 物品附魔前后的页数不同
        var hasRegenerated = false;
        if (toolItemStack.isOf(Items.ENCHANTED_BOOK) && mutable.getEnchantments().size() == 1
                || totalPage != resultPageSize) {
            genEnchantedBookCache();
            currentPage = Math.min(currentPage, totalPage - 1);
            hasRegenerated = true;
        }
        updateEnchantedBookSlots();

        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return hasRegenerated;
    }

    public void initMenu() {
        clearPage();
        clearCache();
        enchantmentsOnCurrentTool.clear();
    }
}
