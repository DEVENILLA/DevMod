package sir.dev.common.entity.ancientInfector;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.entity.dev.DevNavigation;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IDevNavigation;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AncientInfectorMinion extends HostileEntity implements GeoEntity {

    private static final TrackedData<Boolean> TrackedIsTargeting = DataTracker.registerData(AncientInfector.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Byte> SPIDER_FLAGS = DataTracker.registerData(SpiderEntity.class, TrackedDataHandlerRegistry.BYTE);
    public AncientInfector Owner;
    public LivingEntity waterTarget = null;

    public AncientInfectorMinion(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        experiencePoints = 2048;
        this.stepHeight = 1.0f;
        this.moveControl = new AncientInfectorMinionMoveControl(this);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.landNav = new AncientInfectorMinionNavigation(this, world);
        this.spiderNav = new SpiderNavigation(this, world);
        this.waterNav = new SwimNavigation(this, world);
        this.dataTracker.set(SPIDER_FLAGS, (byte)0);
        setPersistent();
    }

    public void setOwner(AncientInfector owner) { Owner = owner; }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45)
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
        this.goalSelector.add(0, new AncientInfectorMinionFollowGoal(this, .8, 3, 5, true));
        this.goalSelector.add(1, new WanderAroundGoal(this, .3));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TrackedIsTargeting, false);
        this.dataTracker.startTracking(SPIDER_FLAGS, (byte)0);
    }

    @Override
    public void tick()
    {
        if (!world.isClient)
        {
            dataTracker.set(TrackedIsTargeting, getTarget() != null);

            if (Owner != null)
            {
                if (!Owner.minions.contains(this)) Owner.minions.add(this);

                if (Owner.isDead()) discard();

                if (Random.create().nextBetween(0, 60) == 5)
                {
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, getX(), getY(), getZ(), 4, .1, .1, .1, .01);

                        Owner.heal(Random.create().nextBetween(1, 4));
                    }
                }
            }
            else
            {
                discard();
            }

            this.landNav.tick();
            this.waterNav.tick();
            this.HandleNavigation();
            this.HandleWaterTarget();
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
            serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, this.getX(), this.getY(), this.getZ(), 2, .2, .2, .2, .35);
        }
        Owner.damage(damageSource, Random.create().nextBetween(2, (int) (getMaxHealth())));
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
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(false);
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

    public SwimNavigation waterNav;
    public MobNavigation landNav;
    public SpiderNavigation spiderNav;

    @Override
    public boolean isPushedByFluids() {
        return !this.isSwimming();
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() && this.isTouchingWater() && this.getUnderwaterTarget() != null)
        {
            this.updateVelocity(0.1f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(.9));
        }
        else
        {
            super.travel(movementInput);
        }
    }

    public void HandleWaterTarget()
    {
        if (Owner != null && Owner.isAlive()) this.waterTarget = Owner;
        else this.waterTarget =null;
    }

    public void HandleNavigation()
    {
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);

        ((IDevNavigation)waterNav).setTargetPos(((IDevNavigation)getNavigation()).getTargetPos());
        ((IDevNavigation)landNav).setTargetPos(((IDevNavigation)getNavigation()).getTargetPos());
        ((IDevNavigation)spiderNav).setTargetPos(((IDevNavigation)getNavigation()).getTargetPos());
        this.landNav.setCanSwim(true);
        this.landNav.setCanWalkOverFences(true);
        this.landNav.setAvoidSunlight(false);
        this.spiderNav.setCanSwim(true);
        this.spiderNav.setCanWalkOverFences(true);
        this.spiderNav.setAvoidSunlight(false);
        this.waterNav.setCanSwim(true);

        this.spiderNav.recalculatePath();
        this.spiderNav.resetRangeMultiplier();

        this.landNav.recalculatePath();
        this.landNav.resetRangeMultiplier();

        this.waterNav.recalculatePath();
        this.waterNav.resetRangeMultiplier();

        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }


    @Override
    public void updateSwimming() {
        if (!this.world.isClient) {

            if (this.canMoveVoluntarily() && this.isTouchingWater() && this.isTargetingUnderwater())
            {
                this.navigation = this.waterNav;
                this.setSwimming(true);
            }
            else
            {
                this.navigation = this.landNav;
                this.setSwimming(false);
            }
        }
    }

    public static class AncientInfectorMinionMoveControl
            extends MoveControl {
        private final AncientInfectorMinion infector;
        public double waterSpeed;
        public double landSpeed;

        public AncientInfectorMinionMoveControl(AncientInfectorMinion dev) {
            super(dev);
            this.infector = dev;
        }

        @Override
        public void moveTo(double x, double y, double z, double speed) {

            super.moveTo(x, y, z, speed);
            waterSpeed = speed*4;
            landSpeed = speed;
        }

        @Override
        public void tick() {
            if (this.infector.isTouchingWater())
            {
                if (infector.getUnderwaterTarget() != null)
                {
                    if (this.infector.isTargetingUnderwater()) {
                        if (this.state != MoveControl.State.MOVE_TO) {
                            this.infector.setMovementSpeed(0.0f);
                            return;
                        }
                        this.infector.setMovementSpeed((float) landSpeed);
                        if (infector.getUnderwaterTarget() != null && infector.getUnderwaterTarget().getY() > this.infector.getY() || this.infector.isTargetingUnderwater()) {
                            this.infector.setVelocity(this.infector.getVelocity().add(0.0, 0.002, 0.0));
                        }
                        double d = this.targetX - this.infector.getX();
                        double e = this.targetY - this.infector.getY();
                        double f = this.targetZ - this.infector.getZ();
                        double g = Math.sqrt(d * d + e * e + f * f);
                        e /= g;
                        float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f;
                        this.infector.setYaw(this.wrapDegrees(this.infector.getYaw(), h, 90.0f));
                        this.infector.bodyYaw = this.infector.getYaw();
                        float i = (float)(this.speed * this.infector.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                        float j = MathHelper.lerp(0.125f, this.infector.getMovementSpeed(), i);
                        this.infector.setMovementSpeed(j);
                        this.infector.setVelocity(this.infector.getVelocity().add((double)j * d * 0.005, (double)j * e * 0.1, (double)j * f * 0.005));
                    }
                    else
                    {
                        this.infector.setMovementSpeed((float) waterSpeed);
                        this.infector.setMovementSpeed(0.0f);
                        if (targetY > infector.getY())
                        {
                            infector.jump();
                        }
                        else
                        {
                            this.infector.setVelocity(this.infector.getVelocity().add(0.0, -0.005, 0.0));
                        }
                        super.tick();
                    }
                }
                else {
                    this.infector.setMovementSpeed((float) waterSpeed);
                    if (!this.infector.onGround)
                    {
                        this.infector.setVelocity(this.infector.getVelocity().add(0.0, -0.002, 0.0));
                    }
                    super.tick();
                }
            }
            else
            {
                this.infector.setMovementSpeed((float) landSpeed);
                super.tick();
            }
        }
    }

    public boolean isTargetingUnderwater()
    {
        if (this.getUnderwaterTarget() == null) return  false;
        return (this.getUnderwaterTarget() != null && (this.getUnderwaterTarget().isSubmergedInWater() || this.getUnderwaterTarget().isTouchingWater()));
    }
    public LivingEntity getUnderwaterTarget()
    {
        if (waterTarget == null) return null;
        if (waterTarget.isDead())
        {
            this.setUnderwaterTarget(null);
            return null;
        }
        return waterTarget;
    }
    public void setUnderwaterTarget(LivingEntity target)
    {
        waterTarget = target;
    }
}
