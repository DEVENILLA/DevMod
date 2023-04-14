package sir.dev.common.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.block.ModBlocks;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.item.dev.SculkTumor;
import software.bernie.example.registry.EntityRegistry;

public class ModItems
{
    public static final Item DEV_ITEM = registerItem("dev_item",
            new DevItem(DevItem.GetSettings()));
    public static final Item SCULK_TUMOR = registerItem("sculk_tumor",
            new SculkTumor(SculkTumor.GetSettings()));
    public static final SpawnEggItem ANCIENT_INFECTOR_SPAWN_EGG = (SpawnEggItem) registerItem("ancient_infector_spawn_egg", new SpawnEggItem(ModEntities.ANCIENT_INFECTOR, 0x26303b, 0x00FFFF, new Item.Settings()));
    public static final SpawnEggItem ULTIMATE_INFECTOR_SPAWN_EGG = (SpawnEggItem) registerItem("ultimate_infector_spawn_egg", new SpawnEggItem(ModEntities.ULTIMATE_INFECTOR, 0xfef9be, 0x00FFFF, new Item.Settings()));


    public static Item registerItem(String name, Item item)
    {
        return Registry.register(Registries.ITEM, new Identifier(DevMod.MOD_ID, name), item);
    }

    public static void register()
    {
        addItemsToItemGroup();
    }

    public static void addItemsToItemGroup() {
        addToItemGroup(ModItemGroup.DEV_TAB, ANCIENT_INFECTOR_SPAWN_EGG);
        addToItemGroup(ModItemGroup.DEV_TAB, ULTIMATE_INFECTOR_SPAWN_EGG);
        addToItemGroup(ModItemGroup.DEV_TAB, ModBlocks.SCULK_MONOLITH.asItem());
        addToItemGroup(ModItemGroup.DEV_TAB, DEV_ITEM);
        addToItemGroup(ModItemGroup.DEV_TAB, SCULK_TUMOR);
    }

    private static void addToItemGroup(ItemGroup group, Item item) {
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(item));
    }
}
