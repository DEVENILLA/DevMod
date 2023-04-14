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
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.AncientInfectorMinion;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
import sir.dev.common.entity.ancientInfector.UltimateInfectorMinion;
import sir.dev.common.entity.dev.DevEntity;

public class ModEntities
{
    public static final EntityType<DevEntity> DEV = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "dev"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<DevEntity> entityType, World world) -> new DevEntity(entityType, world))
                    .dimensions(EntityDimensions.fixed(.4f, .7f))
                    .build()
    );
    public static final EntityType<AncientInfector> ANCIENT_INFECTOR = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "ancient_infector"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<AncientInfector> entityType, World world) -> new AncientInfector(entityType, world))
                    .dimensions(EntityDimensions.fixed(.5f*3, 2.5f))
                    .build()
    );
    public static final EntityType<AncientInfectorMinion> ANCIENT_INFECTOR_MINION = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "ancient_infector_minion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<AncientInfectorMinion> entityType, World world) -> new AncientInfectorMinion(entityType, world))
                    .dimensions(EntityDimensions.fixed(.3f, .4f))
                    .build()
    );
    public static final EntityType<UltimateInfector> ULTIMATE_INFECTOR = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "ultimate_infector"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<UltimateInfector> entityType, World world) -> new UltimateInfector(entityType, world))
                    .dimensions(EntityDimensions.fixed(.5f*3, 2.5f))
                    .build()
    );
    public static final EntityType<UltimateInfectorMinion> ULTIMATE_INFECTOR_MINION = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DevMod.MOD_ID, "ultimate_infector_minion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<UltimateInfectorMinion> entityType, World world) -> new UltimateInfectorMinion(entityType, world))
                    .dimensions(EntityDimensions.fixed(.3f, .4f))
                    .build()
    );



    //registering the mod entities
    public static void register()
    {
        DevMod.LOGGER.debug("registering modded entities for " + DevMod.MOD_ID);

        FabricDefaultAttributeRegistry.register(DEV, DevEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(ANCIENT_INFECTOR, AncientInfector.setAttributes());
        FabricDefaultAttributeRegistry.register(ANCIENT_INFECTOR_MINION, AncientInfectorMinion.setAttributes());
        FabricDefaultAttributeRegistry.register(ULTIMATE_INFECTOR, UltimateInfector.setAttributes());
        FabricDefaultAttributeRegistry.register(ULTIMATE_INFECTOR_MINION, UltimateInfectorMinion.setAttributes());
    }
}
