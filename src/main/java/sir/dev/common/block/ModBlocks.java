package sir.dev.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import software.bernie.example.block.FertilizerBlock;
import software.bernie.geckolib.GeckoLib;

public class ModBlocks
{
    public static final SculkMonolith SCULK_MONOLITH = registerBlock("sculk_monolith", new SculkMonolith());

    public static <B extends Block> B registerBlock(String name, B block) {
        return register(block, new Identifier(DevMod.MOD_ID, name));
    }

    private static <B extends Block> B register(B block, Identifier name) {
        Registry.register(Registries.BLOCK, name, block);
        BlockItem item = new BlockItem(block, (new Item.Settings()));

        item.appendBlocks(Item.BLOCK_ITEMS, item);
        Registry.register(Registries.ITEM, name, item);
        return block;
    }

    public static void register()
    {

    }
}
