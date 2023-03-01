package sir.dev.client.screen.dev;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import sir.dev.DevMod;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;

import java.util.List;
import java.util.function.Predicate;

public class DevScreenHandler extends ScreenHandler
{
    private static final int SIZE = DEV_CONSTS.INV_SIZE;
    private final Inventory inventory;
    private final ItemStack provider;
    private final DevEntity entityHost;

    public DevScreenHandler(int syncId, PlayerInventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleInventory(SIZE), ItemStack.EMPTY, null);
    }

    public DevScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ItemStack stack, DevEntity entityDataSaver)
    {
        super(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE, syncId);
        int j;
        int i;

        this.provider = stack;
        this.entityHost = entityDataSaver;
        DevScreenHandler.checkSize(inventory, SIZE);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.addSlot(new DevScreenSlot(inventory, j + i * 3, 62 + j * 18, 17 + i * 18, provider));
            }
        }

        this.addSlot(new CombatDevScreenSlot(inventory, 9, 62 + 4 * 18, 17 + 0 * 18, provider));
        this.addSlot(new CombatDevScreenSlot(inventory, 10, 62 + 4 * 18, 17 + 2 * 18, provider));

        addPlayerInventorySlots(playerInventory);
        addHotbarSlots(playerInventory);
    }

    public void addHotbarSlots(PlayerInventory playerInventory)
    {
        int i;
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public void addPlayerInventorySlots(PlayerInventory playerInventory)
    {
        int j;
        int i;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot < 9 ? !this.insertItem(itemStack2, 9, 45, true) : !this.insertItem(itemStack2, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot2.onTakeItem(player, itemStack2);
        }
        ApplyChanges();
        return itemStack;
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        ApplyChanges();
        super.updateSlotStacks(revision, stacks, cursorStack);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        ApplyChanges();
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        ApplyChanges();
        this.inventory.onClose(player);
    }

    public void ApplyChanges()
    {
        if (provider != null)
        {
            DefaultedList<ItemStack> InvItems = DefaultedList.of();
            for (int i = 0; i < SIZE; i++)
            {
                InvItems.add(this.inventory.getStack(i));
            }

            if (provider.getNbt() != null)
            {
                Inventories.writeNbt(provider.getNbt(), InvItems);
            }
        }

        if (entityHost != null)
        {
            entityHost.setInventoryStacks(this.inventory);
        }
    }
}
