package sir.dev.common.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.item.dev.DevItem;

public class ModItems
{
    public static final Item DEV_ITEM = registerItem("dev_item",
            new DevItem(DevItem.GetSettings()));

    public static Item registerItem(String name, Item item)
    {
        return Registry.register(Registries.ITEM, new Identifier(DevMod.MOD_ID, name), item);
    }

    public static void register()
    {
        addItemsToItemGroup();
    }

    public static void addItemsToItemGroup() {
        addToItemGroup(ModItemGroup.DEV_TAB, DEV_ITEM);
    }

    private static void addToItemGroup(ItemGroup group, Item item) {
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(item));
    }
}
