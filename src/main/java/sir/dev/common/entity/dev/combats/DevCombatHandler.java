package sir.dev.common.entity.dev.combats;

import com.ibm.icu.text.MessagePattern;
import net.minecraft.block.*;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import sir.dev.common.entity.dev.DevEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DevCombatHandler
{
    public static float OnUseSword(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        if (dev.getTarget() != null) {if (dev.distanceTo(dev.getTarget())>5)return 0;}
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                dev.triggerAnim("other", "leap");
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, dev.getX(), dev.getY(), dev.getZ(), 30, 2, .5, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);

                dev.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                mainStack.use(world, (PlayerEntity) owner, hand);
                mainStack.finishUsing(world, dev);
                mainStack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });

                for (LivingEntity entity : entities)
                {
                    dev.tryAttack(entity);
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    entity.takeKnockback(3, dev.getX(), dev.getZ());
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 3;
    }

    public static float OnUseAxe(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        if (dev.getTarget() != null) {if (dev.distanceTo(dev.getTarget())>5)return 0;}
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, dev.getX(), dev.getY(), dev.getZ(), 60, 2, 1, 2, .35);
                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, dev.getX(), dev.getY(), dev.getZ(), 60, 2, 1, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);
                dev.triggerAnim("other", "attack");
                for (LivingEntity entity : entities)
                {
                    dev.tryAttack(entity);
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    entity.addVelocity(0, .1, 0);
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, (int)(1.5*20), 0));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, (int)(1.5*20), 0));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, (int)(1.5*20), 0));
                    entity.setAttacker(null);
                    entity.setAttacking(null);
                    if (entity instanceof MobEntity mob) { mob.setTarget(null); }
                    if (entity instanceof TameableEntity tamed && tamed.isTamed() && tamed.getOwner() != null)
                    {
                        tamed.getOwner().setAttacking(null);
                        tamed.getOwner().setAttacker(null);
                    }
                }

                mainStack.use(world, (PlayerEntity) owner, hand);
                mainStack.finishUsing(world, dev);
                mainStack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });

                dev.playSound(SoundEvents.BLOCK_ANVIL_HIT, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);

                List<BlockState> blocks = new ArrayList<>();
                List<BlockPos> blockPositions = new ArrayList<>();
                for (int x = -2; x <= 4; x++)
                {
                    for (int z = -2; z <= 4; z++)
                    {
                        for (int y = -1; y <= 0; y++)
                        {
                            if (!(dev.getY()-y <= -64))
                            {
                                if (getRandomFloat(0, 1) > .89f)
                                {
                                    BlockPos pos = new BlockPos(
                                            dev.getX()+x,
                                            dev.getY()+y,
                                            dev.getZ()+z
                                    );
                                    Block block = serverWorld.getBlockState(pos).getBlock();
                                    if (
                                            block != Blocks.BEDROCK &&
                                                    block != Blocks.END_PORTAL &&
                                                    block != Blocks.END_PORTAL_FRAME &&
                                                    block != Blocks.END_GATEWAY &&
                                                    block != Blocks.OBSIDIAN &&
                                                    block != Blocks.NETHER_PORTAL
                                    )
                                    {
                                        blockPositions.add(pos);
                                        blocks.add(serverWorld.getBlockState(pos));
                                    }
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < blocks.size(); i++)
                {
                    BlockState block = blocks.get(i);
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 4;
    }

    public static float OnUseTrident(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {

        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                serverWorld.spawnParticles(ParticleTypes.CLOUD, dev.getX(), dev.getY(), dev.getZ(), 10, .5, .5, .5, .45);

                dev.playSound(SoundEvents.ITEM_TRIDENT_THROW, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                dev.triggerAnim("other", "leap");

                mainStack.use(world, (PlayerEntity) owner, hand);
                mainStack.finishUsing(world, dev);
                mainStack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });

                double xDir = target.getX() - dev.getX();
                double yDir = target.getY() - dev.getY();
                double zDir = target.getZ() - dev.getZ();
                double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                Vec3d Velocity = new Vec3d
                        (xDir/magnitude * 2,
                                yDir/magnitude * 2,
                                zDir/magnitude * 2);

                dev.addVelocity(Velocity);
                dev.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, target.getPos());
                dev.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());

                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(mainStack);

                for (Enchantment enchantment : enchantments.keySet()) {
                    if (enchantment == Enchantments.CHANNELING) {
                        List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);
                        entities.add(target);

                        for (LivingEntity entity : entities)
                        {
                            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(3*20), 0));
                            World world = entity.world;
                            BlockPos pos = entity.getBlockPos();
                            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                            lightning.setCosmetic(true);
                            lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                            entity.damage(DamageSource.LIGHTNING_BOLT, Random.create().nextBetween(0, 10));
                            world.spawnEntity(lightning);
                        }

                        BlockPos pos = dev.getBlockPos();
                        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                        lightning.setCosmetic(true);
                        lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                        dev.ModifyDevCharged(25);
                        world.spawnEntity(lightning);
                    }
                }

            }
        };
        action.execute();
        action.SaveInventoryData();
        return 3;
    }

    public static float OnUseShield(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        if (dev.getTarget() != null) {if (dev.distanceTo(dev.getTarget())>5)return 0;}
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                dev.triggerAnim("other", "leap");
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, dev.getX(), dev.getY(), dev.getZ(), 30, 3, 3, 3, .35);

                dev.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);

                mainStack.use(world, (PlayerEntity) owner, hand);
                mainStack.finishUsing(world, dev);
                mainStack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });

                dev.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int)(1*20), 0));
                owner.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int)(1*20), 0));

                for (LivingEntity entity : entities)
                {
                    double xDir = entity.getX() - dev.getX();
                    double yDir = entity.getY() - dev.getY();
                    double zDir = entity.getZ() - dev.getZ();
                    double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                    Vec3d Velocity = new Vec3d
                            (xDir/magnitude * -2,
                                    yDir/magnitude * -2,
                                    zDir/magnitude * -2);

                    entity.addVelocity(Velocity);
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(3*20), 0));
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 4;
    }

    public static float OnUseTNT(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        if (dev.getTarget() != null) {if (dev.distanceTo(dev.getTarget())>5)return 0;}
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                if (dev.distanceTo(target) > 3.8)
                dev.triggerAnim("other", "backflip");
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, dev.getX(), dev.getY(), dev.getZ(), 30, 2, .5, 2, .35);
                dev.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, dev, dev.getPos(), 4, 1, 4);

                mainStack.decrement(1);
                DamageSource dmg = DamageSource.mob(dev);

                for (LivingEntity entity : entities)
                {
                    entity.damage(dmg, Random.create().nextBetween(20, 60));
                    serverWorld.createExplosion(dev, entity.getX(), entity.getY(), entity.getZ(), 1, World.ExplosionSourceType.MOB);
                    double xDir = entity.getX() - dev.getX();
                    double yDir = entity.getY() - dev.getY();
                    double zDir = entity.getZ() - dev.getZ();
                    double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                    Vec3d Velocity = new Vec3d
                            (xDir/magnitude * -2,
                                    yDir/magnitude * -2,
                                    zDir/magnitude * -2);

                    entity.addVelocity(Velocity);
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 4.3F;
    }

    public static float OnUseBow(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                int arrowStack = -62672571;
                for (int i = 0; i<inventory.size(); i++)
                {
                    if (inventory.getStack(i).getItem() instanceof ArrowItem)
                    {
                        arrowStack = i;
                        break;
                    }
                }
                if (arrowStack != -62672571)
                {

                    dev.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);

                    mainStack.use(world, (PlayerEntity) owner, hand);
                    mainStack.finishUsing(world, dev);

                    this.shootArrow(arrowStack);

                    inventory.getStack(arrowStack).decrement(1);
                    mainStack.damage(Random.create().nextBetween(1, 9), dev, new Consumer<DevEntity>() {
                        @Override
                        public void accept(DevEntity devEntity) {

                        }
                    });
                }
                else
                {
                    owner.sendMessage(Text.literal("your dev doesn't have arrows"));
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 3;
    }

    public static float OnUseCrossbow(DevEntity dev, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        
        DevCombatAction action = new DevCombatAction(dev, MainStack, OtherStack, hand){
            @Override
            public void execute() {
                super.execute();
                int arrowStack = -62672571;
                for (int i = 0; i<inventory.size(); i++)
                {
                    if (inventory.getStack(i).getItem() instanceof ArrowItem)
                    {
                        arrowStack = i;
                        break;
                    }
                }
                if (arrowStack != -62672571) {

                    dev.playSound( SoundEvents.ENTITY_ARROW_SHOOT, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);

                    mainStack.use(world, (PlayerEntity) owner, hand);
                    mainStack.finishUsing(world, dev);

                    for (int i = 0; i < 3; i++)
                    {
                        PersistentProjectileEntity arrow = this.shootArrow(arrowStack);
                        arrow.setPunch(3);
                        arrow.setPierceLevel((byte)3);
                    }

                    inventory.getStack(arrowStack).decrement(1);
                    mainStack.damage(Random.create().nextBetween(1, 9), dev, new Consumer<DevEntity>() {
                        @Override
                        public void accept(DevEntity devEntity) {

                        }
                    });
                }
                else
                {
                    owner.sendMessage(Text.literal("your dev doesn't have arrows"));
                }
            }
        };
        action.execute();
        action.SaveInventoryData();
        return 1;
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

    public static float getRandomFloat(float val1, float val2)
    {
        int accuracy = 100000;
        return Random.create().nextBetween((int)(val1*accuracy), (int)(val2*accuracy))/accuracy;
    }
}
