package sir.dev.common.item.dev;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import sir.dev.DevMod;
import sir.dev.client.item.dev.DevItemRenderer;
import sir.dev.client.screen.dev.*;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevHealthState;
import sir.dev.common.util.DevState;
import sir.dev.common.util.IEntityDataSaver;
import software.bernie.example.client.renderer.item.JackInTheBoxRenderer;
import software.bernie.example.registry.SoundRegistry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DevItem extends Item implements GeoItem {

    public DevItem(Settings settings)
    {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static Settings GetSettings()
    {
        Settings s = new FabricItemSettings();

        s.fireproof();
        s.maxCount(1);
        s.maxDamage(5);
        s.rarity(Rarity.EPIC);

        return s;
    }

    public static int getHP(ItemStack stack)
    {
        if (!stack.getNbt().contains(DEV_CONSTS.NBT_KEY_HP))
        {
            stack.getNbt().putInt(DEV_CONSTS.NBT_KEY_HP, DEV_CONSTS.MAX_HP);
        }

        return stack.getNbt().getInt(DEV_CONSTS.NBT_KEY_HP);
    }

    public static boolean getAIControlled(ItemStack stack)
    {
        if (!stack.getNbt().contains(DEV_CONSTS.NBT_KEY_AI_CONTROL))
        {
            stack.getNbt().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, true);
        }

        return stack.getNbt().getBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL);
    }

    public static Inventory getInventory(NbtCompound nbt)
    {
        DefaultedList<ItemStack> InventoryStacks = DefaultedList.ofSize(DEV_CONSTS.INV_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, InventoryStacks);
        Inventory newInv = new SimpleInventory(DEV_CONSTS.INV_SIZE);
        for (int i = 0; i < InventoryStacks.size(); i++)
        {
            newInv.setStack(i, InventoryStacks.get(i));
        }
        return newInv;
    }

    public static DevState getState(NbtCompound nbt)
    {
        DevState state = DevState.getDefault();

        if (!nbt.contains(DEV_CONSTS.NBT_KEY_STATE))
        {
            nbt.putString(DEV_CONSTS.NBT_KEY_STATE, state.toString());
        }
        else
        {
            state = DevState.valueOf(nbt.getString(DEV_CONSTS.NBT_KEY_STATE));
        }

        return state;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getHP(stack) < DEV_CONSTS.MAX_HP;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(Lerp(0, 13, (float)getHP(stack) / (float)DEV_CONSTS.MAX_HP));
    }

    float Lerp(float a, float b, float f)
    {
        return (float) (a * (1.0 - f) + (b * f));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.packRgb(DEV_CONSTS.HEALTH_BAR_COLOR, .15f, .3f);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        DevState curState = getState(stack.getNbt());
        int curHP = getHP(stack);
        Inventory inv = getInventory(stack.getNbt());

        tooltip.add(Text.literal(
                "§lState§r : " + curState.toString()
        ));

        tooltip.add(Text.literal(
                "§l§4Health§f§r : (" + curHP + " §l/§r " + DEV_CONSTS.MAX_HP + ") "
        ));

        //tooltip.add(Text.literal(
        //        "§l§2Controller§f§r : " + ((getAIControlled(stack) == true) ? "AI" : "user")
        //));

        if (!inv.isEmpty())
        {
            tooltip.add(Text.literal(
                    "§lInventory Contents§r :"
            ));

            for (int i = 0; i < inv.size(); i++)
            {
                if (i < 9)
                {
                    ItemStack item = inv.getStack(i);
                    if (item.isEmpty()) continue;
                    String itemInfo = item.getName().getString() + " (" + item.getCount() + ")";
                    tooltip.add(Text.literal(
                            itemInfo
                    ));
                }
                else
                {
                    ItemStack item = inv.getStack(i);
                    if (item.isEmpty()) continue;
                    String itemInfo = "§l" + item.getName().getString() + " (" + item.getCount() + ")";
                    tooltip.add(Text.literal(
                            itemInfo
                    ));
                }
            }
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean isNbtSynced() {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!stack.getNbt().contains(DEV_CONSTS.NBT_KEY_HP))
        {
            stack.getNbt().putInt(DEV_CONSTS.NBT_KEY_HP, DEV_CONSTS.MAX_HP);
        }

        if (!stack.getNbt().contains(DEV_CONSTS.NBT_KEY_STATE))
        {
            stack.getNbt().putString(DEV_CONSTS.NBT_KEY_STATE, DevState.getDefault().toString());
        }

        if (world instanceof ServerWorld serverLevel)
        {
            DevHealthState healthState = DevHealthState.getHealthState(getHP(stack), DEV_CONSTS.MAX_HP);
            if (healthState == DevHealthState.BIT_INFECTED)
            {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, serverLevel), "main", "bit");
            }
            else if (healthState == DevHealthState.NEAR_INFECTED)
            {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, serverLevel), "main", "near");
            }
            else if (healthState == DevHealthState.ALMOST_INFECTED)
            {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, serverLevel), "main", "almost");
            }
            else if (healthState == DevHealthState.COMPLETELY_INFECTED)
            {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, serverLevel), "main", "complete");
            }
            else
            {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, serverLevel), "main", "normal");
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockEntity blockEntity;
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        if (DevItem.PlayerHasDevAlive(context.getPlayer()) == true)  {
            return ActionResult.SUCCESS;
        }
        ItemStack itemStack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockState blockState = world.getBlockState(blockPos);
        BlockPos blockPos2 = blockState.getCollisionShape(world, blockPos).isEmpty() ? blockPos : blockPos.offset(direction);
        EntityType<?> entityType2 = ModEntities.DEV;
        DevEntity spawned = (DevEntity)entityType2.spawnFromItemStack((ServerWorld)world, itemStack, context.getPlayer(), blockPos2, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP);
        if (spawned != null) {
            SaveDevToPlayer(context.getPlayer(), spawned);
            spawned.setDevAIcontrol(getAIControlled(itemStack));
            spawned.setOwner(context.getPlayer());
            spawned.setHealth(getHP(itemStack));

            NbtCompound nbt = spawned.getPersistentData();

            spawned.setState(getState(itemStack.getNbt()));
            spawned.setInventoryStacks(getInventory(itemStack.getNbt()));

            spawned.setPersistentData(nbt);

            itemStack.decrement(1);
            world.emitGameEvent((Entity)context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        return ActionResult.CONSUME;
    }

    public static void SaveDevToPlayer(PlayerEntity player, DevEntity dev)
    {
        World world = player.getWorld();

        if (world.isClient()) return;

        IEntityDataSaver playerData = (IEntityDataSaver)player;

        playerData.getPersistentData().putUuid(DEV_CONSTS.NBT_KEY_OWNED_DEV, dev.getUuid());
    }

    public static boolean PlayerHasDevAlive(PlayerEntity player)
    {
        if (GetDevFromPlayer(player) != null) return true;
        return false;
    }

    public static DevEntity GetDevFromPlayer(PlayerEntity player)
    {
        World world = player.getWorld();

        IEntityDataSaver playerData = (IEntityDataSaver)player;
        if (playerData.getPersistentData().contains(DEV_CONSTS.NBT_KEY_OWNED_DEV))
        {
            if (world instanceof ServerWorld serverWorld)
            {
                Entity dev = serverWorld.getEntity(playerData.getPersistentData().getUuid(DEV_CONSTS.NBT_KEY_OWNED_DEV));

                if ( dev != null && dev instanceof DevEntity devEntity && devEntity.isAlive())
                {
                    return devEntity;
                }
            }
        }

        return null;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient())
        {
            NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return DevItem.this.createMenu(syncId, inv, player, user.getStackInHand(hand));
                }

                @Override
                public Text getDisplayName() {
                    return DevItem.this.getInventoryDisplayName();
                }
            };

            if (screenHandlerFactory != null)
            {
                user.openHandledScreen(screenHandlerFactory);
            }
        }
        return super.use(world, user, hand);
    }

    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player, ItemStack stack) {
        if (DevHealthState.getHealthState(getHP(stack), DEV_CONSTS.MAX_HP) == DevHealthState.BIT_INFECTED)
        {
            return new Damaged1DevScreenHandler(
                    syncId,
                    player.getInventory(),
                    getInventory(stack.getNbt()),
                    stack,
                    null
            );
        }
        else if (DevHealthState.getHealthState(getHP(stack), DEV_CONSTS.MAX_HP) == DevHealthState.NEAR_INFECTED)
        {
            return new Damaged2DevScreenHandler(
                    syncId,
                    player.getInventory(),
                    getInventory(stack.getNbt()),
                    stack,
                    null
            );
        }
        else if (DevHealthState.getHealthState(getHP(stack), DEV_CONSTS.MAX_HP) == DevHealthState.ALMOST_INFECTED)
        {
            return new Damaged3DevScreenHandler(
                    syncId,
                    player.getInventory(),
                    getInventory(stack.getNbt()),
                    stack,
                    null
            );
        }
        else if (DevHealthState.getHealthState(getHP(stack), DEV_CONSTS.MAX_HP) == DevHealthState.COMPLETELY_INFECTED)
        {
            return new Damaged4DevScreenHandler(
                    syncId,
                    player.getInventory(),
                    getInventory(stack.getNbt()),
                    stack,
                    null
            );
        }
        else
        {
            return new NormalDevScreenHandler(
                    syncId,
                    player.getInventory(),
                    getInventory(stack.getNbt()),
                    stack,
                    null
            );
        }
    }

    public Text getInventoryDisplayName() {
        return Text.literal("Dev's Inventory");
    }

    private static final RawAnimation NORMAL_IDLE_ANIM = RawAnimation.begin().thenLoop("normal");
    private static final RawAnimation BIT_IDLE_ANIM = RawAnimation.begin().thenLoop("bit");
    private static final RawAnimation NEAR_IDLE_ANIM = RawAnimation.begin().thenLoop("near");
    private static final RawAnimation ALMOST_IDLE_ANIM = RawAnimation.begin().thenLoop("almost");
    private static final RawAnimation COMPLETE_IDLE_ANIM = RawAnimation.begin().thenLoop("complete");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    // Utilise our own render hook to define our custom renderer
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private DevItemRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new DevItemRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "main", 0, state -> PlayState.STOP)
                .triggerableAnim("normal", NORMAL_IDLE_ANIM)
                .triggerableAnim("bit", BIT_IDLE_ANIM)
                .triggerableAnim("near", NEAR_IDLE_ANIM)
                .triggerableAnim("almost", ALMOST_IDLE_ANIM)
                .triggerableAnim("complete", COMPLETE_IDLE_ANIM)
        );
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
