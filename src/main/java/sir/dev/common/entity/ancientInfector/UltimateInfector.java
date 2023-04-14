package sir.dev.common.entity.ancientInfector;

import io.netty.channel.FixedRecvByteBufAllocator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.task.SonicBoomTask;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
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
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.SculkTumor;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IDevNavigation;
import sir.dev.common.util.IFallingBlock;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class UltimateInfector extends VexEntity implements GeoEntity {

    private static final TrackedData<Boolean> TrackedIsTargeting = DataTracker.registerData(UltimateInfector.class, TrackedDataHandlerRegistry.BOOLEAN);
    private ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(Text.literal("The Sculk Cell"), BossBar.Color.RED, BossBar.Style.PROGRESS).setDarkenSky(true);

    public List<UltimateInfectorMinion> minions = new ArrayList<>();
    public List<FallingBlockEntity> fallingBlocks = new ArrayList<>();

    public UltimateInfector(EntityType<? extends UltimateInfector> entityType, World world) {
        super(entityType, world);
        experiencePoints = 2048;
        this.stepHeight = 1.0f;
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        if (world instanceof ServerWorld)
        {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.setWeather(20, 20, true, true);
        }
        setPersistent();
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 512)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 2048)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 5)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 10)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1);
    }

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new ChargeTargetGoal());
        this.goalSelector.add(0, new MeleeAttackGoal((PathAwareEntity) this, .8f, false));
        this.goalSelector.add(1, new LookAroundGoal((MobEntity) this));
        this.goalSelector.add(2, new WanderAroundGoal((PathAwareEntity) this, .3f));
        this.goalSelector.add(3, new LookAtEntityGoal((MobEntity) this, PlayerEntity.class, 5));
    }

    class ChargeTargetGoal
            extends Goal {
        public ChargeTargetGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = UltimateInfector.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive() && !UltimateInfector.this.getMoveControl().isMoving() && UltimateInfector.this.random.nextInt(UltimateInfector.ChargeTargetGoal.toGoalTicks(7)) == 0) {
                return UltimateInfector.this.squaredDistanceTo(livingEntity) > 45.0;
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return UltimateInfector.this.getMoveControl().isMoving() && UltimateInfector.this.isCharging() && UltimateInfector.this.getTarget() != null && UltimateInfector.this.getTarget().isAlive() && UltimateInfector.this.random.nextBetween(0, 50) != 49;
        }

        @Override
        public void start() {
            LivingEntity livingEntity = UltimateInfector.this.getTarget();
            if (livingEntity != null) {
                Vec3d vec3d = livingEntity.getEyePos();
                UltimateInfector.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
            }
            UltimateInfector.this.setCharging(true);
            UltimateInfector.this.playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0f, 1.0f);
        }

        @Override
        public void stop() {
            UltimateInfector.this.setCharging(false);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = UltimateInfector.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            if (UltimateInfector.this.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                UltimateInfector.this.tryAttack(livingEntity);
                UltimateInfector.this.setCharging(false);
            } else {
                double d = UltimateInfector.this.squaredDistanceTo(livingEntity);
                if (d < 9) {
                    Vec3d vec3d = livingEntity.getEyePos();
                    UltimateInfector.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
                }
            }
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TrackedIsTargeting, false);
    }

    @Override
    public void tick()
    {
        if (!world.isClient)
        {
            dataTracker.set(TrackedIsTargeting, getTarget() != null);
            HandleCustomCombat();
            this.HandleTarget();
            HandleStates();
            this.bossBar.setDarkenSky(true);
            this.bossBar.setThickenFog(true);
            this.bossBar.setVisible(true);
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        }
        //SuckBlocks();

        super.tick();
    }

    private void SuckBlocks()
    {
        if (!world.isClient)
        {
            if (random.nextBetween(0, random.nextBetween(10, 30)) == 5) {
                BreakRandomBlock();

                List<FallingBlockEntity> additionalBlocks = new ArrayList<>();

                additionalBlocks = this.world.getEntitiesByClass(
                        FallingBlockEntity.class,
                        Box.of(this.getPos(), 20, 20, 20),
                        livingEntity -> {
                            return true;
                        }
                );

                for (FallingBlockEntity add : additionalBlocks) {
                    if (!fallingBlocks.contains(add)) fallingBlocks.add(add);
                }
            }
        }

        for (int i =0; i < fallingBlocks.size(); i++)
        {
            FallingBlockEntity block = fallingBlocks.get(i);
            if (block == null)
            {
                fallingBlocks.remove(i);
            }
        }

        for (int i =0; i < fallingBlocks.size(); i++)
        {
            FallingBlockEntity block = fallingBlocks.get(i);
            ((IFallingBlock)block).setInfector(this);
            block.noClip = true;
            block.setOnGround(false);
            block.setHurtEntities(1, 5);
            block.setInvulnerable(true);
            block.setNoGravity(true);
            double xDir = block.getX() - this.getX();
            double yDir = block.getY() - this.getY();
            double zDir = block.getZ() - this.getZ();
            double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
            Vec3d Velocity = new Vec3d
                    (xDir/magnitude * -.1,
                            yDir/magnitude * -.1,
                            zDir/magnitude * -.1);

            block.move(MovementType.SELF, Velocity);
            block.setVelocity(Velocity);

            if (distanceTo(block) <= getWidth())
            {
                this.heal(Random.create().nextBetween(1, 4));
                ((IFallingBlock) block).setInfector(null);
                block.noClip = false;
                block.setNoGravity(false);
                block.discard();
                fallingBlocks.remove(i);
            }
        }
    }

    public void BreakRandomBlock()
    {
        int times = 15;

        for (int i = 0; i < times; i++)
        {
            int x = Random.create().nextBetween(-10, 10);
            int y = Random.create().nextBetween(-10, 10);
            int z = Random.create().nextBetween(-10, 10);

            BlockPos pos = new BlockPos(getBlockX()+x, getBlockY()+y, getBlockZ()+z);
            BlockState state = world.getBlockState(pos);

            if (state.isAir() || state.getHardness(world, pos) < 0)
            {

            }
            else
            {
                FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, pos, state);
                ((IFallingBlock)fallingBlock).setInfector(this);
                fallingBlock.noClip = true;
                fallingBlock.setOnGround(false);
                fallingBlock.setNoGravity(true);
                fallingBlocks.add(fallingBlock);
                break;
            }
        }

    }

    public void HandleTarget()
    {
        if (this.getTarget() != null)
        {
            if (random.nextBetween(0, 200) == 153)
            {
                if (this.getAttacker() != null)
                {
                    if (this.getAttacker().isAlive() && random.nextBetween(0, 3) == 2)
                        this.setTarget(this.getAttacker());
                    else
                        ChooseTarget();
                }
                else
                {
                    ChooseTarget();
                }
            }
        }
        else
        {

            if (this.getAttacker() != null)
            {
                if (this.getAttacker().isAlive() && random.nextBetween(0, 3) == 2)
                    this.setTarget(this.getAttacker());
                else
                    ChooseTarget();
            }
            else
            {
                ChooseTarget();
            }
        }
    }

    public void ChooseTarget()
    {
        List<LivingEntity> entities = new ArrayList<>();
        List<PlayerEntity> players = new ArrayList<>();
        List<DevEntity> devs = new ArrayList<>();
        if (world instanceof ServerWorld serverWorld) {
            entities = serverWorld.getEntitiesByClass(
                    LivingEntity.class,
                    Box.of(getPos(), 256, world.getHeight(), 256),
                    livingEntity -> {
                        if (livingEntity instanceof AncientInfectorMinion || livingEntity instanceof AncientInfector || livingEntity instanceof UltimateInfector)
                            return false;
                        if (livingEntity instanceof DevEntity || livingEntity instanceof PlayerEntity) return false;
                        return true;
                    }
            );
            devs = serverWorld.getEntitiesByClass(
                    DevEntity.class,
                    Box.of(getPos(), 256, world.getHeight(), 256),
                    livingEntity -> {
                        return true;
                    }
            );
            players = serverWorld.getEntitiesByClass(
                    PlayerEntity.class,
                    Box.of(getPos(), 256, world.getHeight(), 256),
                    livingEntity -> {
                        if (livingEntity.isSpectator() || livingEntity.isCreative()) return false;
                        return true;
                    }
            );
        }

        if (devs.size() > 0 && players.size() > 0)
        {
            if (Random.create().nextBetween(0, 100) >= 50)
            {
                this.setTarget(players.get(random.nextBetween(0, players.size()-1)));
            }
            else
            {
                this.setTarget(devs.get(random.nextBetween(0, devs.size()-1)));
            }
        }
        else if (devs.size() > 0 && players.size() == 0)
        {
            this.setTarget(devs.get(random.nextBetween(0, devs.size()-1)));
        }
        else if (devs.size() == 0 && players.size() > 0)
        {
            this.setTarget(players.get(random.nextBetween(0, players.size()-1)));
        }
        else if (devs.size() == 0 && players.size() == 0)
        {
            if (entities.size() > 1)
            {
                this.setTarget(DEV_CONSTS.getClosestEntity(entities, this));
            }
        }
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
        this.triggerAnim("other", "death");
        if (world instanceof ServerWorld serverWorld)
        {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 30, 2, .5, 2, .35);
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 30, 2, .5, 2, .35);
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
            this.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
            List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 4, 1, 4);
            serverWorld.createExplosion(this, this.getX(), this.getY(), this.getZ(), 3, World.ExplosionSourceType.MOB);
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
        ItemEntity item = new ItemEntity(world, getX(), getY(), getZ(), new ItemStack(ModItems.SCULK_TUMOR, 1));
        world.spawnEntity(item);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }


    @Override
    public boolean cannotDespawn() {
        despawnCounter = 0;
        return  true;
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
    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime >= 150 && !this.world.isClient() && !this.isRemoved()) {
            this.world.sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(RemovalReason.KILLED);
        }
        super.updatePostDeath();
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
        return SoundEvents.ENTITY_WITHER_AMBIENT;
    }
    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
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

    public boolean isTargeting() { return dataTracker.get(TrackedIsTargeting); }

    public void HandleStates()
    {
        BossBar.Color color = BossBar.Color.RED;
        int percent = getHealthPercentage();
        int maxMinionCount = 5;

        for (int i = 0; i < minions.size(); i++)
        {
            UltimateInfectorMinion minion = minions.get(i);
            if (minion == null)
            {
                minions.remove(minion);
            }
            else
            {
                if (minion.isDead()) minions.remove(minion);
            }
        }

        this.bossBar.setColor(color);
    }

    /* ANIMATION HANDLING */

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final RawAnimation SPIN_ANIM = RawAnimation.begin().thenPlay("spin");
    public static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {

            RawAnimation IdleAnimation = RawAnimation.begin().thenLoop("idle");
            return state.setAndContinue(IdleAnimation);

        }).setSoundKeyframeHandler(event -> {

        }));

        controllers.add(new AnimationController<>(this, "other", 0, animationState -> PlayState.CONTINUE)
                .triggerableAnim("spin", SPIN_ANIM)
                .triggerableAnim("death", DEATH_ANIM)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    //CUSTOM COMBAT
    public int chance = 100;

    public void HandleCustomCombat()
    {
        if (this.getTarget() == null) return;

        if (chance == 0) chance = 100;

        if (random.nextBetween(0, chance) == chance-1)
        {
            ServerWorld serverWorld = (ServerWorld) this.world;
            int attackIndex = random.nextBetween(0, 4);
            if (attackIndex == 2)
            {
                LivingEntity target = this.getTarget();

                if (target == null)
                {
                    chance = 1;
                }
                else
                {
                    serverWorld.spawnParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 10, .5, .5, .5, .45);

                    this.playSound(SoundEvents.ITEM_TRIDENT_THROW, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                    double xDir = target.getX() - this.getX();
                    double yDir = target.getY() - this.getY();
                    double zDir = target.getZ() - this.getZ();
                    double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                    Vec3d Velocity = new Vec3d
                            (xDir/magnitude * 2,
                                    yDir/magnitude * 2,
                                    zDir/magnitude * 2);

                    this.addVelocity(Velocity);
                    this.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, target.getPos());
                    this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
                    chance = 100;
                }
            } // trident attack
            else if (attackIndex == 3)
            {
                this.triggerAnim("other", "spin");
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 30, 3, 3, 3, .35);

                this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 4, 1, 4);

                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int)(1*20), 0));

                for (LivingEntity entity : entities)
                {
                    double xDir = entity.getX() - this.getX();
                    double yDir = entity.getY() - this.getY();
                    double zDir = entity.getZ() - this.getZ();
                    double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                    Vec3d Velocity = new Vec3d
                            (xDir/magnitude * -2,
                                    yDir/magnitude * -2,
                                    zDir/magnitude * -2);

                    entity.addVelocity(Velocity);
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(3*20), 0));
                }
                chance = 90;
            } // shield attack
            else if (attackIndex == 4)
            {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 30, 2, .5, 2, .35);
                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 20, 20, 20);
                serverWorld.createExplosion(this, this.getX(), this.getY(), this.getZ(), 1.5f, World.ExplosionSourceType.MOB);

                DamageSource dmg = DamageSource.mob(this);

                for (LivingEntity entity : entities)
                {
                    double d = this.getX();
                    double e = this.getBodyY(.9);
                    double f = this.getZ();
                    double g = entity.getX() - d;
                    double h = entity.getY() - e;
                    double i = entity.getZ() - f;
                    WitherSkullEntity witherSkullEntity = new WitherSkullEntity(this.world, this, g, h, i);
                    witherSkullEntity.setOwner(this);
                    if (random.nextBetween(0, 100) > 75) {
                        witherSkullEntity.setCharged(true);
                    }
                    witherSkullEntity.setPos(d, e, f);
                    this.world.spawnEntity(witherSkullEntity);
                }
                chance = 120;
            } // tnt attack
        }
    }


    public List<LivingEntity> getAffectableEntitiesInARange(ServerWorld world, Vec3d anchorPos, double sizeX, double sizeY, double sizeZ)
    {
        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                Box.of(anchorPos, sizeX, sizeY, sizeZ),
                livingEntity -> {
                    if (livingEntity == this || livingEntity == null || livingEntity.isDead()) return false;
                    if (livingEntity instanceof AncientInfectorMinion || livingEntity instanceof UltimateInfector) return false;
                    return true;
                }
        );
        return entities;
    }

    public int getHealthPercentage()
    {
        return  (int)((getHealth()/getMaxHealth())*100);
    }
}
