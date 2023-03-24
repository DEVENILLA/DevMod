package sir.dev.client.screen.dev;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import sir.dev.DevMod;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;

import java.util.List;

public class DevScreenHandler extends ScreenHandler
{
    private static final int SIZE = DEV_CONSTS.INV_SIZE;
    private final Inventory inventory;
    private final ItemStack provider;
    private final DevEntity entityHost;

    public World world;

    public Identifier TEX()
    {
        return new Identifier(DevMod.MOD_ID, "textures/gui/container/dev_inventory.png");
    }

    public DevScreenHandler(int syncId, PlayerInventory playerInventory)
    {
        this(
                syncId,
                playerInventory,
                new SimpleInventory(SIZE),
                ItemStack.EMPTY,
                null,
                ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_NORMAL
        );
    }

    public DevScreenHandler(int syncId, @NotNull PlayerInventory playerInventory, Inventory inventory, ItemStack stack, DevEntity entityDataSaver, ScreenHandlerType type)
    {
        super(type, syncId);
        world = playerInventory.player.getWorld();
        int j;
        int i;

        this.provider = stack;
        this.entityHost = entityDataSaver;
        DevScreenHandler.checkSize(inventory, SIZE);
        this.inventory = inventory;
        updateToClient();
        sendContentUpdates();
        inventory.onOpen(playerInventory.player);
        updateToClient();

        this.addSlot(new Slot(inventory, 0, 62+1, 26+1-16));
        this.addSlot(new Slot(inventory, 1, 62+1, 44+1-16));
        this.addSlot(new Slot(inventory, 2, 62+1, 62+1-16));
        this.addSlot(new Slot(inventory, 3, 80+1, 26+1-16));
        this.addSlot(new Slot(inventory, 4, 80+1, 44+1-16));
        this.addSlot(new Slot(inventory, 5, 80+1, 62+1-16));
        this.addSlot(new Slot(inventory, 6, 98+1, 26+1-16));
        this.addSlot(new Slot(inventory, 7, 98+1, 44+1-16));
        this.addSlot(new Slot(inventory, 8, 98+1, 62+1-16));

        this.addSlot(new CombatDevScreenSlot(inventory, 9, 122+1, 85+1-16, provider));
        this.addSlot(new CombatDevScreenSlot(inventory, 10, 40+1, 85+1-16, provider));

        addPlayerInventorySlots(playerInventory);
        addHotbarSlots(playerInventory);
    }

    public void addHotbarSlots(PlayerInventory playerInventory)
    {
        int i;
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142 + 16));
        }
    }

    public void addPlayerInventorySlots(PlayerInventory playerInventory)
    {
        int j;
        int i;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 16));
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
