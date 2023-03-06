package sir.dev.common.entity.dev.combats;

import com.ibm.icu.text.MessagePattern;
import net.minecraft.block.*;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import sir.dev.common.entity.dev.DevEntity;

import java.util.ArrayList;
import java.util.List;

public class DevCombatHandler
{
    public static float OnUseSword(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, dev.getX(), dev.getY(), dev.getZ(), 30, 2, .5, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);

                for (LivingEntity entity : entities)
                {
                    dev.tryAttack(entity);
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    serverWorld.playSound(dev.getX(), dev.getY(), dev.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 100, 1, true);
                    entity.takeKnockback(3, dev.getX(), dev.getZ());
                }
            }
        };
        action.execute();
        return 3;
    }

    public static float OnUseAxe(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, dev.getX(), dev.getY(), dev.getZ(), 30, 2, 1, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);

                for (LivingEntity entity : entities)
                {
                    dev.tryAttack(entity);
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    entity.addVelocity(0, 1, 0);
                }

                serverWorld.playSound(dev.getX(), dev.getY(), dev.getZ(), SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.HOSTILE, 100, 1, true);

                List<BlockState> blocks = new ArrayList<>();
                List<BlockPos> blockPositions = new ArrayList<>();
                for (int x = -2; x <= 4; x++)
                {
                    for (int z = -2; z <= 4; z++)
                    {
                        BlockPos pos = new BlockPos(
                                dev.getX()-x,
                                dev.getY()-1,
                                dev.getZ()-z
                        );
                        blockPositions.add(pos);
                        Block block = serverWorld.getBlockState(pos).getBlock();
                        if (block != Blocks.BEDROCK && block != Blocks.END_PORTAL && block != Blocks.END_GATEWAY)
                        {
                            blocks.add(serverWorld.getBlockState(pos));
                        }
                    }
                }
                for (int i = 0; i < blocks.size(); i++)
                {
                    BlockPos pos = new BlockPos(blockPositions.get(i).getX(), blockPositions.get(i).getY(), blockPositions.get(i).getZ());
                    FallingBlockEntity falling = new FallingBlockEntity(EntityType.FALLING_BLOCK, world);
                    world.setBlockState(pos, serverWorld.getBlockState(pos).getFluidState().getBlockState(), Block.SKIP_DROPS);
                    falling.setPos(pos.getX(), pos.getY()+.5, pos.getZ());
                    falling.addVelocity(0, (Random.create().nextBetween(50, 120) / 100),0);
                    world.spawnEntity(falling);
                }
            }
        };
        action.execute();
        return 4;
    }

    public static float OnUseTrident(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                serverWorld.spawnParticles(ParticleTypes.CLOUD, dev.getX(), dev.getY(), dev.getZ(), 10, .5, .5, .5, .45);

                double xDir = target.getX() - dev.getX();
                double yDir = target.getY() - dev.getY();
                double zDir = target.getZ() - dev.getZ();
                double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                Vec3d Velocity = new Vec3d
                        (xDir/magnitude * 1.5,
                                yDir/magnitude * 1.5,
                                zDir/magnitude * 1.5);

                dev.setVelocity(Velocity);
                dev.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, target.getPos());
                dev.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
            }
        };
        action.execute();
        return 3;
    }

    public static float OnUseShield(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        return 3;
    }

    public static float OnUseTNT(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        return 3;
    }

    public static float OnUseBow(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        return 3;
    }

    public static float OnUseCrossbow(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        return 3;
    }

    public static List<LivingEntity> getAffectableEntitiesInARange(ServerWorld world, DevEntity dev, Vec3d anchorPos, double sizeX, double sizeY, double sizeZ)
    {
        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                Box.of(anchorPos, sizeX, sizeY, sizeZ),
                livingEntity -> {
                    if (livingEntity == dev || livingEntity == dev.getOwner()) return false;
                    if (livingEntity instanceof TameableEntity tamed && tamed.isTamed() && tamed.getOwner() == dev.getOwner()) return false;
                    return true;
                }
        );
        return entities;
    }
}
