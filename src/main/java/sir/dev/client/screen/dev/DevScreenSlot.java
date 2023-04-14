package sir.dev.client.screen.dev;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.DevItem;

import java.util.function.Predicate;

public class DevScreenSlot extends Slot
{

    public DevScreenSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.getItem().equals(ModItems.DEV_ITEM)) return false;
        if (stack.getItem() instanceof DevItem) return false;
        return super.canInsert(stack);
    }
}
