package sir.dev.common.entity.ancientInfector;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IDevNavigation;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.UUID;

public class UltimateInfectorMinion extends VexEntity implements GeoEntity {

    public AncientInfector Owner;
    public LivingEntity waterTarget = null;

    public UltimateInfectorMinion(EntityType<? extends UltimateInfectorMinion> entityType, World world) {
        super(entityType, world);
        experiencePoints = 2048;
        this.stepHeight = 1.0f;
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        setPersistent();
        noClip = true;
        setNoGravity(true);
    }

    public void setOwner(AncientInfector owner) { Owner = owner; }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
                .add(EntityAttributes.GENERIC_ARMOR, 1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 2048)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 5)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 10)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1);
    }

    @Override
    protected void initGoals()
    {

    }

    @Override
    public void tick()
    {
        if (!world.isClient)
        {
            if (Owner != null)
            {
                if (Owner.isDead()) discard();

                if (Random.create().nextBetween(0, 30) == 5)
                {
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, getX(), getY(), getZ(), 5, .1, .1, .1, .01);
                    }
                }

                this.setTarget(Owner.getTarget());

                if (getTarget() == null)
                {
                    discard();
                    return;
                }
                if (getTarget().isDead())
                {
                    discard();
                    return;
                }

                if (distanceTo(getTarget()) > 2)
                {
                    getMoveControl().moveTo(getTarget().getX(), getTarget().getY(), getTarget().getZ(), 1.5);
                }
                else
                {
                    if (Owner != null)
                    {
                        if (Owner.isAlive())
                        {
                            LightningEntity lighting = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                            lighting.setPos(getTarget().getX(), getTarget().getY(), getTarget().getZ());
                            lighting.setCosmetic(true);
                            lighting.setOnFire(true);
                            getTarget().damage(DamageSource.mob(Owner), Random.create().nextBetween(1, 5));
                            world.spawnEntity(lighting);
                            Owner.setPosition(getTarget().getPos());
                            Owner.flying = null;
                            discard();
                            return;
                        }
                        else
                        {
                            discard();
                            return;
                        }
                    }
                    else
                    {
                        discard();
                        return;
                    }
                }
            }
            else
            {
                discard();
            }
        }

        super.tick();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity entity = damageSource.getAttacker();
        LivingEntity livingEntity = this.getPrimeAdversary();
        if (this.scoreAmount >= 0 && livingEntity != null) {
            livingEntity.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        if (world instanceof ServerWorld serverWorld)
        {
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 30, .2, .2, .2, .35);
        }
        this.dead = true;
        this.getDamageTracker().update();
        if (this.world instanceof ServerWorld) {
            if (entity == null || entity.onKilledOther((ServerWorld)this.world, this)) {
                this.emitGameEvent(GameEvent.ENTITY_DIE);
                this.drop(damageSource);
                this.onKilledBy(livingEntity);
            }
            this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        }
    }


    @Override
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound( SoundEvents.ENTITY_WITHER_SKELETON_STEP, .15f, 1.0f );
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    public SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON;
    }
    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source == DamageSource.FALL)
            return false;
        if (source == DamageSource.DROWN)
            return false;
        if (source == DamageSource.LAVA)
            return false;
        if (source == DamageSource.WITHER)
            return false;
        if (source == DamageSource.LIGHTNING_BOLT)
            return false;
        if (source.getDeathMessage(this).equals("witherSkull"))
            return false;

        return super.damage(source, amount);
    }

    private NbtCompound PersistentData;

    public NbtCompound getPersistentData() {
        if (this.PersistentData == null)
        {
            this.PersistentData = new NbtCompound();
        }
        return this.PersistentData;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (this.PersistentData != null)
        {
            nbt.put(DEV_CONSTS.NBT_KEY_DATA, this.PersistentData);
        }
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains(DEV_CONSTS.NBT_KEY_DATA, NbtElement.COMPOUND_TYPE))
        {
            this.PersistentData = nbt.getCompound(DEV_CONSTS.NBT_KEY_DATA);
        }
        super.readNbt(nbt);
    }

    /* ANIMATION HANDLING */

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {

            if (state.isMoving())
            {
                RawAnimation MoveAnimation = RawAnimation.begin().thenLoop("move");
                return state.setAndContinue(MoveAnimation);
            }

            RawAnimation IdleAnimation = RawAnimation.begin().thenLoop("idle");
            return state.setAndContinue(IdleAnimation);

        }).setSoundKeyframeHandler(event -> {

        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }
}
