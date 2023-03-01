package sir.dev.client.screen.dev;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import sir.dev.common.item.ModItems;

import java.util.function.Predicate;

public class DevScreenSlot extends Slot
{
    private final ItemStack parentInventoryItem;

    public DevScreenSlot(Inventory inventory, int index, int x, int y, ItemStack parentInventoryItem) {
        super(inventory, index, x, y);
        this.parentInventoryItem = parentInventoryItem;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.getItem().equals(ModItems.DEV_ITEM)) return false;
        return super.canInsert(stack);
    }
}
