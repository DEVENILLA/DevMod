package sir.dev.common.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;

public class ModEntities
{
    public static final EntityType<DevEntity> DEV = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "dev"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<DevEntity> entityType, World world) -> new DevEntity(entityType, world))
                    .dimensions(EntityDimensions.fixed(.4f, .7f))
                    .build()
    );



    //registering the mod entities
    public static void register()
    {
        DevMod.LOGGER.debug("registering modded entities for " + DevMod.MOD_ID);

        FabricDefaultAttributeRegistry.register(DEV, DevEntity.setAttributes());
    }
}
