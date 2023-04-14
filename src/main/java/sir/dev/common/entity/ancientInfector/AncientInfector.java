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
import sir.dev.common.entity.ModEntities;
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

public class AncientInfector extends HostileEntity implements GeoEntity {

    private static final TrackedData<Boolean> TrackedIsTargeting = DataTracker.registerData(AncientInfector.class, TrackedDataHandlerRegistry.BOOLEAN);
    private ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(Text.literal("The Sculk Reaper"), BossBar.Color.PURPLE, BossBar.Style.PROGRESS).setDarkenSky(true);
    private static final TrackedData<Byte> SPIDER_FLAGS = DataTracker.registerData(SpiderEntity.class, TrackedDataHandlerRegistry.BYTE);

    public LivingEntity waterTarget = null;
    public List<ItemEntity> blocks = new ArrayList<>();
    public List<AncientInfectorMinion> minions = new ArrayList<>();
    public UltimateInfectorMinion flying = null;
    public BlockPos previousPos = null;

    public AncientInfector(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        experiencePoints = 2048;
        this.setHealth(this.getMaxHealth()/2);
        this.stepHeight = 1.0f;
        this.moveControl = new AncientInfectorMoveControl(this);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.landNav = new AncientInfectorNavigation(this, world);
        this.spiderNav = new SpiderNavigation(this, world);
        this.waterNav = new SwimNavigation(this, world);
        this.dataTracker.set(SPIDER_FLAGS, (byte)0);
        if (world instanceof ServerWorld)
        {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.setWeather(20, 20, true, true);
        }
        setPersistent();
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
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024)
                .add(EntityAttributes.GENERIC_ARMOR, 3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 2048)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 5)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 10)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1);
    }

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new MeleeAttackGoal((PathAwareEntity) this, .8f, false));
        this.goalSelector.add(1, new LookAroundGoal((MobEntity) this));
        this.goalSelector.add(2, new WanderAroundGoal((PathAwareEntity) this, .3f));
        this.goalSelector.add(3, new LookAtEntityGoal((MobEntity) this, PlayerEntity.class, 5));
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
            if (previousPos == null) previousPos = this.getBlockPos();
            dataTracker.set(TrackedIsTargeting, getTarget() != null);
            HandleCustomCombat();
            this.HandleTarget();
            HandleStates();
            this.bossBar.setDarkenSky(true);
            this.bossBar.setThickenFog(true);
            this.bossBar.setVisible(true);
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
            this.HandleWaterTarget();

            if (flying != null)
            {
                if (flying.isDead()) flying = null;
            }
            else
            {
                if (Random.create().nextBetween(0, 150) == 9)
                {
                    flying = new UltimateInfectorMinion(ModEntities.ULTIMATE_INFECTOR_MINION, world);
                    flying.setPos(getX(), getY(), getZ());
                    flying.setOwner(this);
                    world.spawnEntity(flying);
                }
            }


            List<BlockState> blocks = new ArrayList<>();
            List<BlockPos> blockPositions = new ArrayList<>();
            if (world instanceof ServerWorld serverWorld)
            {
                for (int x = -1; x <= 1; x++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        for (int y = 0; y <= 2; y++)
                        {
                            BlockPos pos = new BlockPos(
                                    this.getX()+x,
                                    this.getY()+y,
                                    this.getZ()+z
                            );
                            Block block = serverWorld.getBlockState(pos).getBlock();
                            if (block.getHardness() > 0)
                            {
                                blockPositions.add(pos);
                                blocks.add(serverWorld.getBlockState(pos));
                            }
                        }
                    }
                }
                for (int i = 0; i < blocks.size(); i++)
                {
                    BlockState block = blocks.get(i);
                    world.breakBlock(blockPositions.get(i), false, this);
                }
            }

            previousPos = this.getBlockPos();
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
        for (ItemEntity drop : blocks)
        {
            drop.noClip = false;
            drop.setNoGravity(false);
        }
        this.triggerAnim("other", "death");
        if (world instanceof ServerWorld serverWorld)
        {
            for (int i = -64; i < world.getTopY(); i+=2)
            {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), i, this.getZ(), 60, 5, 5, 5, .35);
                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                serverWorld.createExplosion(this, this.getX(), i, this.getZ(), 6, World.ExplosionSourceType.MOB);
            }
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

        UltimateInfector minion = new UltimateInfector(ModEntities.ULTIMATE_INFECTOR, world);
        minion.setPos(getX(), getY(), getZ());
        world.spawnEntity(minion);

        discard();
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
    public void onAttacking(Entity target) {
        this.triggerAnim("other", "attack");
        super.onAttacking(target);
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
            this.remove(Entity.RemovalReason.KILLED);
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
        BossBar.Color color = BossBar.Color.WHITE;
        int percent = getHealthPercentage();
        int maxMinionCount = 5;
        if (percent > 65)
        {
            color = BossBar.Color.PINK;
        }
        if (percent > 75)
        {
            color = BossBar.Color.PURPLE;
        }
        if (percent > 85)
        {
            color = BossBar.Color.RED;
        }

        for (int i = 0; i < minions.size(); i++)
        {
            AncientInfectorMinion minion = minions.get(i);
            if (minion == null)
            {
                minions.remove(minion);
            }
            else
            {
                if (minion.isDead()) minions.remove(minion);
            }
        }

        if (Random.create().nextBetween(0, 260) == 5 && minions.size() < maxMinionCount)
        {
            AncientInfectorMinion minion = new AncientInfectorMinion(ModEntities.ANCIENT_INFECTOR_MINION, world);
            minion.setPos(getX(), getY(), getZ());
            minion.setOwner(this);
            world.spawnEntity(minion);
        }

        this.bossBar.setColor(color);
    }

    /* ANIMATION HANDLING */

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    public static final RawAnimation SPIN_ANIM = RawAnimation.begin().thenPlay("spin");
    public static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {

            if (state.isMoving())
            {
                if (state.getAnimatable().isTargeting())
                {
                    RawAnimation MoveAnimation = RawAnimation.begin().thenLoop("run");
                    return state.setAndContinue(MoveAnimation);
                }
                RawAnimation MoveAnimation = RawAnimation.begin().thenLoop("walk");
                return state.setAndContinue(MoveAnimation);
            }

            RawAnimation IdleAnimation = RawAnimation.begin().thenLoop("idle");
            return state.setAndContinue(IdleAnimation);

        }).setSoundKeyframeHandler(event -> {

        }));

        controllers.add(new AnimationController<>(this, "other", 0, animationState -> PlayState.CONTINUE)
                .triggerableAnim("attack", ATTACK_ANIM)
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
            if (attackIndex == 0)
            {
                this.triggerAnim("other", "spin");
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(), this.getZ(), 30, 2, .5, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 4, 1, 4);

                this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);

                for (LivingEntity entity : entities)
                {
                    this.tryAttack(entity);
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    entity.takeKnockback(3, this.getX(), this.getZ());
                }
                chance = 100;
            } // sword attack
            else if (attackIndex == 1)
            {
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 60, 2, 1, 2, .35);
                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), 60, 2, 1, 2, .35);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 4, 1, 4);
                this.triggerAnim("other", "attack");
                for (LivingEntity entity : entities)
                {
                    this.tryAttack(entity);
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

                this.playSound(SoundEvents.BLOCK_ANVIL_HIT, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);

                List<BlockState> blocks = new ArrayList<>();
                List<BlockPos> blockPositions = new ArrayList<>();
                List<BlockPos> falling = new ArrayList<>();
                for (int x = -2; x <= 2; x++)
                {
                    for (int z = -2; z <= 2; z++)
                    {
                        for (int y = -1; y <= 1; y++)
                        {
                            if (!(this.getY()-y <= -64))
                            {
                                BlockPos pos = new BlockPos(
                                        this.getX()+x,
                                        this.getY()+y,
                                        this.getZ()+z
                                );
                                Block block = serverWorld.getBlockState(pos).getBlock();
                                if (block.getHardness() > 0)
                                {
                                    blockPositions.add(pos);
                                    blocks.add(serverWorld.getBlockState(pos));
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < blocks.size(); i++)
                {
                    BlockState block = blocks.get(i);
                    if (random.nextBetween(0, 100) > 50)
                    {
                        world.breakBlock(blockPositions.get(i), true, this);
                    }
                    else
                    {
                        falling.add(blockPositions.get(i));
                    }
                }
                for (int i = 0; i < falling.size(); i++)
                {
                    BlockState block = world.getBlockState(falling.get(i));
                    FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, falling.get(i), block);
                }
                chance = 120;
            } // axe attack
            else if (attackIndex == 2)
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
                    this.triggerAnim("other", "spin");

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
                    this.tryAttack(entity);
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
                this.triggerAnim("other", "attack");
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 30, 2, .5, 2, .35);
                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f);
                List<LivingEntity> entities = getAffectableEntitiesInARange(serverWorld, this.getPos(), 4, 1, 4);
                serverWorld.createExplosion(this, this.getX(), this.getY(), this.getZ(), 1, World.ExplosionSourceType.MOB);

                DamageSource dmg = DamageSource.mob(this);

                for (LivingEntity entity : entities)
                {
                    entity.damage(dmg, Random.create().nextBetween(20, 60));
                    serverWorld.createExplosion(this, entity.getX(), entity.getY(), entity.getZ(), 0, World.ExplosionSourceType.MOB);
                    double xDir = entity.getX() - this.getX();
                    double yDir = entity.getY() - this.getY();
                    double zDir = entity.getZ() - this.getZ();
                    double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                    Vec3d Velocity = new Vec3d
                            (xDir/magnitude * -2,
                                    yDir/magnitude * -2,
                                    zDir/magnitude * -2);

                    entity.addVelocity(Velocity);
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
                    if (livingEntity instanceof AncientInfectorMinion || livingEntity instanceof AncientInfector) return false;
                    return true;
                }
        );
        return entities;
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
        if (this.getTarget() != null && this.getTarget().isAlive()) this.waterTarget = this.getTarget();
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

    public static class AncientInfectorMoveControl
            extends MoveControl {
        private final AncientInfector infector;
        public double waterSpeed;
        public double landSpeed;

        public AncientInfectorMoveControl(AncientInfector dev) {
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
    public int getHealthPercentage()
    {
        return  (int)((getHealth()/getMaxHealth())*100);
    }
}
