package sir.dev.common.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import sir.dev.DevMod;
import sir.dev.common.block.ModBlocks;
import software.bernie.example.block.entity.FertilizerBlockEntity;
import software.bernie.example.registry.BlockRegistry;
import software.bernie.geckolib.GeckoLib;

public class ModBlockEntities
{
    public static final BlockEntityType<SculkMonolithEntity> SCULK_MONOLITH = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            DevMod.MOD_ID + ":sculk_monolith",
            FabricBlockEntityTypeBuilder.create(SculkMonolithEntity::new, ModBlocks.SCULK_MONOLITH).build(null));
}
