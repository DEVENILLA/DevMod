package sir.dev.client.screen.dev;

import net.minecraft.block.TntBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.DevItem;

public class CombatDevScreenSlot extends Slot
{
    private final ItemStack parentInventoryItem;

    public CombatDevScreenSlot(Inventory inventory, int index, int x, int y, ItemStack parentInventoryItem) {
        super(inventory, index, x, y);
        this.parentInventoryItem = parentInventoryItem;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.getItem().equals(ModItems.DEV_ITEM)) return false;
        if (stack.getItem() instanceof DevItem) return false;
        if (!CheckItemCompatibility(stack.getItem())) return false;
        if (stack.getItem() instanceof DevItem) return false;
        return super.canInsert(stack);
    }

    public boolean CheckItemCompatibility(Item i)
    {
        if (i instanceof DevItem) return false;
        return true;
    }

    public static boolean isCompatible(Item i)
    {
        if (i instanceof DevItem) return false;
        return true;
    }
}
