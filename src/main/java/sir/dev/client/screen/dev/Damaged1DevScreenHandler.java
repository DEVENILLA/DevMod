package sir.dev.client.screen.dev;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.common.entity.dev.DevEntity;

public class Damaged1DevScreenHandler extends DevScreenHandler
{
    public Damaged1DevScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
    }

    public Damaged1DevScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ItemStack stack, DevEntity entityDataSaver) {
        super( syncId, playerInventory, inventory, stack, entityDataSaver, ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_DMG_1);
    }

    @Override
    public Identifier TEX() {
        return new Identifier(DevMod.MOD_ID, "textures/gui/container/dev_inventory_dmg_1.png");
    }
}
