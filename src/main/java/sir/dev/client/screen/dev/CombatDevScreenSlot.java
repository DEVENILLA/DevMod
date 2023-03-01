package sir.dev.client.screen.dev;

import net.minecraft.block.TntBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import sir.dev.common.item.ModItems;

public class CombatDevScreenSlot extends Slot
{
    private final ItemStack parentInventoryItem;

    public CombatDevScreenSlot(Inventory inventory, int index, int x, int y, ItemStack parentInventoryItem) {
        super(inventory, index, x, y);
        this.parentInventoryItem = parentInventoryItem;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!CheckItemCompatibility(stack.getItem())) return false;
        return super.canInsert(stack);
    }

    public boolean CheckItemCompatibility(Item i)
    {
        if (i instanceof SwordItem) return true;
        if (i instanceof AxeItem) return true;
        if (i instanceof BowItem) return true;
        if (i instanceof CrossbowItem) return true;
        if (i instanceof ShieldItem) return true;
        if (i instanceof TridentItem) return true;
        return false;
    }
}
