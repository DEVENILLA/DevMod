package sir.dev.common.entity.dev;

import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import sir.dev.client.hud.dev.DevHudOverlay;
import sir.dev.client.screen.dev.DevScreenHandler;
import sir.dev.common.entity.dev.combats.DevCombatHandler;
import sir.dev.common.entity.dev.goals.*;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevHealthState;
import sir.dev.common.util.DevState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DevEntity extends TameableEntity implements GeoEntity {

    private static final TrackedData<ItemStack> TrackedMainHandItem = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<ItemStack> TrackedOffHandItem = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<String> TrackedDevState = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> TrackedDevCalled = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TrackedDevAI = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> TrackedMainCooldown = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TrackedOffCooldown = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TrackedParticleEffectsAnimTickrate = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final float CHASE_DISTANCE = 64;
    public static final float TARGET_DISTANCE = 20;

    public DevEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        experiencePoints = 400;
        setPersistent();
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, DEV_CONSTS.MAX_HP)
                .add(EntityAttributes.GENERIC_ARMOR, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, CHASE_DISTANCE)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new DevFollowOwnerWhenCalledGoal(this, 1.5, .75F, .25F, true, false));
        this.goalSelector.add(2, new DevFollowOwnerGoal(this, 1.2, CHASE_DISTANCE, 2F, true,false, false));
        this.goalSelector.add(3, new DevMeleeAttackGoal((PathAwareEntity) this, 1.5f,.1f, false));
        this.goalSelector.add(4, new DevFollowOwnerGoal(this, 1.0, 5F, 2F, false,false, false));
        this.goalSelector.add(5, new DevLookAroundGoal((MobEntity) this));
        this.goalSelector.add(6, new DevWanderAroundGoal((PathAwareEntity) this, 1f));
        this.goalSelector.add(7, new DevLookAtEntityGoal((MobEntity) this, PlayerEntity.class, 5));
        this.goalSelector.add(8, new DevSwimAroundGoal((PathAwareEntity) this, 1.0, 1));
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TrackedMainHandItem, ItemStack.EMPTY);
        this.dataTracker.startTracking(TrackedOffHandItem, ItemStack.EMPTY);
        this.dataTracker.startTracking(TrackedDevState, DevState.getDefault().name());
        this.dataTracker.startTracking(TrackedDevCalled, false);
        this.dataTracker.startTracking(TrackedDevAI, true);
        this.dataTracker.startTracking(TrackedMainCooldown, 80);
        this.dataTracker.startTracking(TrackedParticleEffectsAnimTickrate, -5);
        this.dataTracker.startTracking(TrackedOffCooldown, 120);
        super.initDataTracker();
    }

    @Override
    public void tick() {
        super.tick();
        this.world.getWorldChunk(this.getBlockPos()).setLoadedToWorld(true);
        this.world.getWorldChunk(this.getBlockPos()).loadEntities();
        this.world.getWorldChunk(this.getBlockPos()).setShouldRenderOnUpdate(true);

        this.setHandEffectAnimTickrate(this.getHandEffectAnimTickrate()+1);

        if (!this.world.isClient())
        {
            this.HandleStatesWhenTick();
            this.dataTracker.set(TrackedMainHandItem, this.getInventory().getStack(9));
            this.dataTracker.set(TrackedOffHandItem, this.getInventory().getStack(10));
            this.dataTracker.set(TrackedDevState, this.getState().name());

            this.dataTracker.set(TrackedMainCooldown, this.dataTracker.get(TrackedMainCooldown)-1);
            this.dataTracker.set(TrackedOffCooldown, this.dataTracker.get(TrackedOffCooldown)-1);

            if (IsDevAIcontrolled())
            {
                if (this.getMainCooldown() <= 0 && DevHudOverlay.canUse(this, this.getMainHandStack()))
                {
                    if (random.nextBetween(1, 32) == 5) UseItemInHand(Hand.MAIN_HAND);
                }

                if (this.getOffCooldown() <= 0 && DevHudOverlay.canUse(this, this.getOffHandStack()))
                {
                    if (random.nextBetween(1, 32) == 5) UseItemInHand(Hand.OFF_HAND);
                }
            }

            if (this.getDevState() == DevState.sitting)
            {
                this.setTarget(null);
            }

            if (getOwner() == null)
            {
                ArrayList<PlayerEntity> players = (ArrayList<PlayerEntity>) this.world.getPlayers();
                for (PlayerEntity player : players)
                {
                    if (DevItem.GetDevFromPlayer(player) != null && DevItem.GetDevFromPlayer(player) == this)
                    {
                        this.setOwner(player);
                    }
                }
            }
            else
            {
                DevItem.SaveDevToPlayer(((PlayerEntity)this.getOwner()), this);
            }

            HandleTargeting();
        }
    }

    @Override
    public void checkDespawn() {
        this.world.getWorldChunk(this.getBlockPos()).setLoadedToWorld(true);
        this.world.getWorldChunk(this.getBlockPos()).loadEntities();
        this.despawnCounter = 0;
    }
    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    public void HandleTargeting()
    {
        if (this.world.isClient) return;
        if (getOwner() == null) return;

        if (this.world instanceof ServerWorld serverWorld)
        {
            if (this.getTarget() == null)
            {
                List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                        LivingEntity.class,
                        Box.of(DevEntity.this.getOwner().getPos(), TARGET_DISTANCE, CHASE_DISTANCE*2, TARGET_DISTANCE),
                        mobEntity -> IsViableTarget(mobEntity) == true
                );

                LivingEntity closestTarget = serverWorld.getClosestEntity(
                        entities,
                        TargetPredicate.DEFAULT,
                        this.getOwner(),
                        this.getOwner().getX(),
                        this.getOwner().getY(),
                        this.getOwner().getZ()
                );

                if (closestTarget != null && IsViableTarget(closestTarget))
                {
                    this.setTarget(closestTarget);
                }
            }
            else
            {
                if (!IsViableTarget(this.getTarget()))
                {
                    setTarget(null);
                }
            }
        }
    }

    public boolean IsViableTarget(LivingEntity entity)
    {
        DevEntity dev = DevEntity.this;
        if (dev.getDevState() != DevState.defending || dev.IsDevCalled() == true || dev.getOwner() == null)
        {
            return false;
        }

        if (DEV_CONSTS.GetDistance(entity.getPos(), dev.getPos()) >= CHASE_DISTANCE*2)
        {
            return false;
        }

        if (entity == dev)
        {
            return false;
        }

        if (entity instanceof TameableEntity tamed && tamed.isTamed() && tamed.getOwner() != null && tamed.getOwner() == dev.getOwner())
        {
            return false;
        }

        if (entity instanceof MobEntity mob)
        {
            if
            (
                    dev.getOwner().getAttacker() != mob &&
                            dev.getOwner() != mob.getAttacker() &&
                            dev.getOwner() != mob.getTarget() &&
                            dev != mob.getTarget() &&
                            dev.getAttacker() != mob
            )
            {
                return false;
            }
        }

        if (entity == dev.getOwner())
        {
            return false;
        }

        return true;
    }

    public void ClearTargets()
    {
        this.getOwner().setAttacker(null);
        this.getOwner().setAttacking(null);
        this.setAttacker(null);
        this.setTarget(null);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (hand == Hand.MAIN_HAND)
        {
            OnInteraction(player);
        }
        return super.interactAt(player, hitPos, hand);
    }

    public void OnInteraction(PlayerEntity player)
    {
        ItemStack handStack = player.getStackInHand(Hand.MAIN_HAND);

        if (!this.world.isClient() && this.getOwner() == player)
        {
            if (!player.isSneaking() && handStack.getItem() == Items.AIR)
            {
                ItemStack devItem = new ItemStack(ModItems.DEV_ITEM, 1);

                DEV_CONSTS.setInventoryStacks(devItem.getNbt(), this.getInventoryStacks());
                DEV_CONSTS.setState(devItem.getNbt(), this.getState());
                devItem.getNbt().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, this.IsDevAIcontrolled());
                DEV_CONSTS.setHP(devItem.getNbt(), (int)this.getHealth());
                DEV_CONSTS.setOwner(devItem.getNbt(), player.getUuid());

                player.setStackInHand(Hand.MAIN_HAND, devItem);

                discard();
            }
            else
            {
                if (handStack.getItem() == Items.EMERALD_BLOCK && this.getHealth() < DEV_CONSTS.MAX_HP)
                {
                    this.heal(15);
                    if (world instanceof ServerWorld serverWorld)
                    {
                        serverWorld.spawnParticles(
                                ParticleTypes.HEART,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                5,
                                1,
                                1,
                                1,
                                .5f
                        );
                    }
                    handStack.decrement(1);
                    player.setStackInHand(Hand.MAIN_HAND, handStack);
                    if (this.getHealth() >= DEV_CONSTS.MAX_HP) this.setHealth((float) DEV_CONSTS.MAX_HP);
                }
                else
                {
                    NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                        @Nullable
                        @Override
                        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                            return DevEntity.this.createMenu(syncId, player);
                        }

                        @Override
                        public Text getDisplayName() {
                            return DevEntity.this.getInventoryDisplayName();
                        }
                    };

                    if (screenHandlerFactory != null)
                    {
                        player.openHandledScreen(screenHandlerFactory);
                    }
                }
            }
        }
    }


    @Override
    public void onAttacking(Entity target) {
        super.onAttacking(target);
        this.triggerAnim("other", "attack");
        if (this.world.isClient)return;
        if (target instanceof LivingEntity)
        {
            this.HurtUsingItem(this.getMainHandStack(), (LivingEntity) target);
            this.HurtUsingItem(this.getOffHandStack(), (LivingEntity) target);
            this.HandleStatesWhenAttackEntity((LivingEntity) target);
        }
    }

    public void HandleStatesWhenAttackEntity(LivingEntity target)
    {
        if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.NEAR_INFECTED)
        {
            target.takeKnockback(1, this.getX(), this.getZ());
        }
        if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.ALMOST_INFECTED)
        {
            this.world.createExplosion(this, target.getX(), target.getY(), target.getZ(), 0, World.ExplosionSourceType.MOB);
            target.takeKnockback(2, this.getX(), this.getZ());
        }
        else if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.COMPLETELY_INFECTED)
        {
            this.world.createExplosion(this, target.getX(), target.getY(), target.getZ(), 1, World.ExplosionSourceType.MOB);
            Entity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, this.world);
            lightning.setPos(target.getX(), target.getY(), target.getZ());
            this.world.spawnEntity(lightning);
            target.takeKnockback(3, this.getX(), this.getZ());
        }
    }

    public void HandleStatesWhenTick()
    {
        if (this.world instanceof ServerWorld serverWorld)
        {
            if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.NEAR_INFECTED)
            {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 5, .5, .5, .5, .2);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5*20, 1));
            }
            if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.ALMOST_INFECTED)
            {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 10, .5, .5, .5, .2);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5*20, 2));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 5*20, 2));
            }
            else if (DevHealthState.getHealthState(getHealth(), (float)DEV_CONSTS.MAX_HP) == DevHealthState.COMPLETELY_INFECTED)
            {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 10, .5, .5, .5, .2);
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 10, 2, 2, 2, .2);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5*20, 3));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 5*20, 3));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 5*20, 3));
            }
        }
    }

    public void HurtUsingItem(ItemStack itemStack, LivingEntity target)
    {
        Item item = itemStack.getItem();
        DamageSource dmgSource = DamageSource.GENERIC;
        if (this.getOwner() == null) return;
        if (item instanceof BlockItem b && b.getBlock() instanceof TntBlock)
        {
            this.world.createExplosion(this, target.getX(), target.getY(), target.getZ(), 1, World.ExplosionSourceType.MOB);
        }
        else
        {
            itemStack.postHit(target, (PlayerEntity) this.getOwner());
        }
    }

    public void UseItemInHand(Hand handType)
    {
        if (this.world.isClient) return;
        if (this.getTarget() == null) return;
        if (this.getOwner() == null) return;

        ItemStack MainStack = this.getMainHandStack();
        ItemStack OtherStack = this.getOffHandStack();
        if (handType == Hand.OFF_HAND)
        {
            MainStack = this.getOffHandStack();
            OtherStack = this.getMainHandStack();
        }

        Item MainStackItem = MainStack.getItem();
        Item OtherStackItem = OtherStack.getItem();

        float cooldownInSecs = 0;

        if (MainStackItem instanceof SwordItem) cooldownInSecs = DevCombatHandler.OnUseSword(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof AxeItem) cooldownInSecs = DevCombatHandler.OnUseAxe(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof TridentItem) cooldownInSecs = DevCombatHandler.OnUseTrident(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof ShieldItem) cooldownInSecs = DevCombatHandler.OnUseShield(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof BowItem) cooldownInSecs = DevCombatHandler.OnUseBow(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof CrossbowItem) cooldownInSecs = DevCombatHandler.OnUseCrossbow(this, MainStack, OtherStack, handType);
        else if (MainStackItem instanceof BlockItem block && block.getBlock() instanceof TntBlock) cooldownInSecs = DevCombatHandler.OnUseTNT(this, MainStack, OtherStack, handType);

        if (handType == Hand.MAIN_HAND)
        {
            this.setMainCooldown((int)(cooldownInSecs * 20));
        }
        else if (handType == Hand.OFF_HAND)
        {
            this.setOffCooldown((int)(cooldownInSecs * 20));
        }
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float f = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - amount));
        float g = f - amount;
        if (g > 0.0f && g < 3.4028235E37f && source.getAttacker() instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(g * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        float h = this.getHealth();
        if (this.getOwner() != null && this.world.isClient == false)
        {
            if (this.getOwner().isAlive())
            {
                if (h > 5 && h - amount <= 0)
                {
                    ((ServerWorld)this.world).spawnParticles(ParticleTypes.PORTAL, this.getOwner().getX(), this.getOwner().getY(), this.getOwner().getZ(), 40, 0, 0, 0, 1);
                    ItemStack devItem = new ItemStack(ModItems.DEV_ITEM, 1);

                    DEV_CONSTS.setInventoryStacks(devItem.getNbt(), this.getInventoryStacks());
                    DEV_CONSTS.setState(devItem.getNbt(), this.getState());
                    devItem.getNbt().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, this.IsDevAIcontrolled());
                    DEV_CONSTS.setHP(devItem.getNbt(), (int)1);
                    DEV_CONSTS.setOwner(devItem.getNbt(), this.getOwner().getUuid());

                    this.getOwner().setStackInHand(Hand.MAIN_HAND, devItem);

                    discard();
                }
            }
            else
            {
                if (h > 5 && h - amount <= 0)
                {
                    ((ServerWorld)this.world).spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 40, 0, 0, 0, 1);
                    ItemStack devItem = new ItemStack(ModItems.DEV_ITEM, 1);

                    DEV_CONSTS.setInventoryStacks(devItem.getNbt(), this.getInventoryStacks());
                    DEV_CONSTS.setState(devItem.getNbt(), this.getState());
                    devItem.getNbt().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, this.IsDevAIcontrolled());
                    DEV_CONSTS.setHP(devItem.getNbt(), (int)1);
                    DEV_CONSTS.setOwner(devItem.getNbt(), this.getOwner().getUuid());

                    ItemScatterer.spawn(this.world, this.getX(), this.getY(), this.getZ(), devItem);

                    discard();
                }
            }
        }
        this.setHealth(h - amount);
        this.getDamageTracker().onDamage(source, h, amount);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (!this.world.isClient())
        {
            ItemScatterer.spawn(this.world, this, this.getInventory());
        }
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        this.setTarget(null);
        return super.onKilledOther(world, other);
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound( SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, .15f, 1.0f );
    }

    @Override
    public boolean cannotDespawn() {
        return  true;
    }

    @Override
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    public SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
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

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public boolean isPlayerStaring(PlayerEntity player) {
        Vec3d vec3d = player.getRotationVec(1.0F).normalize();
        Vec3d vec3d2 = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d = vec3d2.length();
        vec3d2 = vec3d2.normalize();
        double e = vec3d.dotProduct(vec3d2);
        return e > 1.0D - 0.025D / d ? player.canSee(this) : false;
    }

    @Override
    public ItemStack getMainHandStack() {
        return this.dataTracker.get(TrackedMainHandItem);
    }

    @Override
    public ItemStack getOffHandStack() {
        return this.dataTracker.get(TrackedOffHandItem);
    }

    public DevState getDevState()
    {
        return DevState.valueOf(this.dataTracker.get(TrackedDevState));
    }

    private NbtCompound PersistentData;

    public NbtCompound getPersistentData() {
        if (this.PersistentData == null)
        {
            this.PersistentData = new NbtCompound();
        }
        return this.PersistentData;
    }

    public Boolean IsDevCalled() { return this.dataTracker.get(TrackedDevCalled); }

    public void setDevCalled(Boolean val) { this.dataTracker.set(TrackedDevCalled, val); }

    public Boolean IsDevAIcontrolled() { return this.dataTracker.get(TrackedDevAI); }

    public void setDevAIcontrol(Boolean val) { this.dataTracker.set(TrackedDevAI, val); }

    public int getMainCooldown() { return this.dataTracker.get(TrackedMainCooldown); }
    public void setMainCooldown(int ticks) { this.dataTracker.set(TrackedMainCooldown, ticks); }

    public int getOffCooldown() { return this.dataTracker.get(TrackedOffCooldown); }
    public void setOffCooldown(int ticks) { this.dataTracker.set(TrackedOffCooldown, ticks); }

    public void setPersistentData(NbtCompound persistentData) {
        this.PersistentData = persistentData;
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
        if (this.getOwnerUuid() != null) {
            this.getPersistentData().putUuid("Owner", this.getOwnerUuid());
        }
        this.getPersistentData().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, dataTracker.get(TrackedDevAI));
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains(DEV_CONSTS.NBT_KEY_DATA, NbtElement.COMPOUND_TYPE))
        {
            this.PersistentData = nbt.getCompound(DEV_CONSTS.NBT_KEY_DATA);
        }
        readDevNBT();
        super.readNbt(nbt);
    }

    public void readDevNBT()
    {
        UUID uUID;
        if (this.getPersistentData().contains(DEV_CONSTS.NBT_KEY_AI_CONTROL)) {
            dataTracker.set(TrackedDevAI, this.getPersistentData().getBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL));
        }
        if (this.getPersistentData().containsUuid("Owner")) {
            uUID = this.getPersistentData().getUuid("Owner");
        } else {
            String string = this.getPersistentData().getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }
        if (uUID != null) {
            try {
                this.setOwnerUuid(uUID);
                this.setTamed(true);
            } catch (Throwable throwable) {
                this.setTamed(false);
            }
        }
    }

    public Inventory getInventory()
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(DEV_CONSTS.INV_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(this.getPersistentData(), itemStacks);
        Inventory inv = new SimpleInventory(DEV_CONSTS.INV_SIZE);
        for (int i = 0; i < DEV_CONSTS.INV_SIZE; i++)
        {
            inv.setStack(i, itemStacks.get(i));
        }
        return inv;
    }

    public DefaultedList<ItemStack> getInventoryStacks()
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(DEV_CONSTS.INV_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(this.getPersistentData(), itemStacks);
        return itemStacks;
    }

    public DevState getState()
    {
        DevState state = DevState.getDefault();

        if (!this.getPersistentData().contains(DEV_CONSTS.NBT_KEY_STATE, NbtElement.STRING_TYPE))
        {
            this.getPersistentData().putString(DEV_CONSTS.NBT_KEY_STATE, DevState.getDefault().name());
        }

        return DevState.valueOf(this.getPersistentData().getString(DEV_CONSTS.NBT_KEY_STATE));
    }

    public UUID getOwnerUUID()
    {
        UUID owner = null;

        if (this.getPersistentData().containsUuid(DEV_CONSTS.NBT_KEY_OWNER)) owner = this.getPersistentData().getUuid(DEV_CONSTS.NBT_KEY_OWNER);

        return owner;
    }

    public void setInventoryStacks(DefaultedList<ItemStack> list)
    {
        Inventories.writeNbt(this.getPersistentData(), list);
    }

    public void setInventoryStacks(Inventory list)
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(DEV_CONSTS.INV_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < DEV_CONSTS.INV_SIZE; i++)
        {
            itemStacks.set(i, list.getStack(i));
        }
        Inventories.writeNbt(this.getPersistentData(), itemStacks);
    }

    public void setState(DevState VAL)
    {
        this.getPersistentData().putString(DEV_CONSTS.NBT_KEY_STATE, VAL.name());
    }


    public void setOwnerUUID(UUID VAL)
    {
        this.getPersistentData().putUuid(DEV_CONSTS.NBT_KEY_OWNER, VAL);
    }

    public ScreenHandler createMenu(int syncId, PlayerEntity player)
    {
        return new DevScreenHandler(
                syncId,
                player.getInventory(),
                this.getInventory(),
                ItemStack.EMPTY,
                this
        );
    }

    public Text getInventoryDisplayName() {
        return Text.literal("Dev's Inventory");
    }

    /* ANIMATION HANDLING */

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    public static final RawAnimation LEAP_ANIM = RawAnimation.begin().thenPlay("spin");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            DevEntity dev = (DevEntity) state.getAnimatable();
            if (dev.getDevState() == DevState.sitting)
            {
                RawAnimation SitAnimation = RawAnimation.begin().thenLoop("sit");
                return state.setAndContinue(SitAnimation);
            }

            if (state.isMoving())
            {
                RawAnimation MoveAnimation = RawAnimation.begin().thenLoop("walk");
                return state.setAndContinue(MoveAnimation);
            }

            RawAnimation IdleAnimation = RawAnimation.begin().thenLoop("idle");
            return state.setAndContinue(IdleAnimation);

        }).setSoundKeyframeHandler(event -> {

        }));

        controllers.add(new AnimationController<>(this, "other", 5, animationState -> PlayState.CONTINUE)
                .triggerableAnim("attack", ATTACK_ANIM)
                .triggerableAnim("leap", LEAP_ANIM)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void setHandEffectAnimTickrate(int rate)
    {
        this.dataTracker.set(TrackedParticleEffectsAnimTickrate, rate);
    }

    public int getHandEffectAnimTickrate()
    {
        return this.dataTracker.get(TrackedParticleEffectsAnimTickrate);
    }
}
