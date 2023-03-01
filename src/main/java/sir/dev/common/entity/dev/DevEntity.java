package sir.dev.common.entity.dev;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sir.dev.client.screen.dev.DevScreenHandler;
import sir.dev.common.item.ModItems;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class DevEntity extends TameableEntity implements GeoEntity {

    private static final TrackedData<ItemStack> TrackedMainHandItem = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<ItemStack> TrackedOffHandItem = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<String> TrackedDevState = DataTracker.registerData(DevEntity.class, TrackedDataHandlerRegistry.STRING);

    public DevEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        experiencePoints = 400;
        setPersistent();
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150)
                .add(EntityAttributes.GENERIC_ARMOR, 20)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStop();
            }
        });
        this.goalSelector.add(4, new SwimGoal(this)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStop();
            }
        });
        this.goalSelector.add(5, new SitGoal(this)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStop();
            }
        });
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStop();
            }
        });
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStop();
            }
        });
        this.goalSelector.add(8, new LookAroundGoal(this)
        {
            @Override
            public boolean canStart() {
                if (DevEntity.this.getState() == DevState.sitting) return false;
                return super.canStart();
            }

            @Override
            public boolean canStop() {
                if (DevEntity.this.getState() == DevState.sitting) return true;
                return super.canStop();
            }
        });
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TrackedMainHandItem, ItemStack.EMPTY);
        this.dataTracker.startTracking(TrackedOffHandItem, ItemStack.EMPTY);
        this.dataTracker.startTracking(TrackedDevState, DevState.getDefault().name());
        super.initDataTracker();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isClient())
        {
            this.dataTracker.set(TrackedMainHandItem, this.getInventory().getStack(9));
            this.dataTracker.set(TrackedOffHandItem, this.getInventory().getStack(10));
            this.dataTracker.set(TrackedDevState, this.getState().name());
        }
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
                DEV_CONSTS.setHP(devItem.getNbt(), (int)this.getHealth());
                DEV_CONSTS.setOwner(devItem.getNbt(), player.getUuid());

                player.setStackInHand(Hand.MAIN_HAND, devItem);

                discard();
            }
            else
            {
                if (handStack.getItem() == Items.EMERALD_BLOCK)
                {
                    heal(15);
                    for (int i = 0; i < 30; i++)
                    {
                        double randomX = random.nextBetween(25, 150) / 100;
                        double randomY = random.nextBetween(25, 150) / 100;
                        double randomZ = random.nextBetween(25, 150) / 100;
                        world.addParticle(ParticleTypes.HEART, this.getX()+randomX, this.getY()+randomY, this.getZ()+randomZ, 0, .1f, 0);
                    }
                    handStack.decrement(1);
                    player.setStackInHand(Hand.MAIN_HAND, handStack);
                }
                else if (handStack.getItem() == Items.STICK)
                {
                    if (this.getState() == DevState.sitting)
                    {
                        this.setState(DevState.following);
                    }
                    else
                    {
                        this.setState(DevState.sitting);
                    }
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
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (!this.world.isClient())
        {
            ItemScatterer.spawn(this.world, this, this.getInventory());
        }
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
        if (source.getDeathMessage(this).equals("witherSkull"))
            return false;

        return super.damage(source, amount);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
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

    public void setPersistentData(NbtCompound persistentData) {
        this.PersistentData = persistentData;
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
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
