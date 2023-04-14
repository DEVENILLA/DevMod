package sir.dev.common.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.block.ModBlocks;

public class ModItemGroup
{
    public static ItemGroup DEV_TAB;

    public static void register()
    {
        DEV_TAB = FabricItemGroup.builder(new Identifier(DevMod.MOD_ID, "devtab"))
                .displayName(Text.literal("Devenilla's Expansion"))
                .icon(() -> new ItemStack(ModItems.SCULK_TUMOR))
                .build();
    }
}
