package sir.dev.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;

import java.util.UUID;

public class DEV_CONSTS
{
    public static final int INV_SIZE = 11;
    public static final int MAX_HP = 150;
    public static final float HEALTH_BAR_COLOR = 1.0f;

    //NBTS
    public static final String NBT_KEY_HP = "HP";
    public static final String NBT_KEY_OWNER = "Owner";
    public static final String NBT_KEY_STATE = "STATE";
    public static final String NBT_KEY_AI_CONTROL = "AIC";
    public static final String NBT_KEY_DATA = "devmod.data";
    public static final String NBT_KEY_OWNED_DEV = "curDev";

    public static Inventory getInventory(NbtCompound nbt)
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, itemStacks);
        Inventory inv = new SimpleInventory(INV_SIZE);
        for (int i = 0; i < INV_SIZE; i++)
        {
            inv.setStack(i, itemStacks.get(i));
        }
        return inv;
    }

    public static double GetDistance(Vec3d vec1, Vec3d vec2)
    {
        double dis = 0;

        dis = Math.sqrt(
                Math.pow(vec1.x - vec2.x, 2) +  Math.pow(vec1.y - vec2.y, 2) + Math.pow(vec1.z - vec2.z, 2)
        );

        return dis;
    }

    public static DefaultedList<ItemStack> getInventoryStacks(NbtCompound nbt)
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, itemStacks);
        return itemStacks;
    }

    public static DevState getState(NbtCompound nbt)
    {
        DevState state = DevState.getDefault();

        if (nbt.contains(NBT_KEY_STATE, NbtElement.STRING_TYPE)) state = DevState.valueOf(nbt.getString(NBT_KEY_STATE));

        return state;
    }

    public static int getHP(NbtCompound nbt)
    {
        int hp = MAX_HP;

        if (nbt.contains(NBT_KEY_HP, NbtElement.INT_TYPE)) hp = nbt.getInt(NBT_KEY_HP);

        return hp;
    }

    public static UUID getOwner(NbtCompound nbt)
    {
        UUID owner = null;

        if (nbt.containsUuid(NBT_KEY_OWNER)) owner = nbt.getUuid(NBT_KEY_OWNER);

        return owner;
    }

    public static void setInventoryStacks(NbtCompound nbt, DefaultedList<ItemStack> list)
    {
        Inventories.writeNbt(nbt, list);
    }

    public static void setInventoryStacks(NbtCompound nbt, Inventory list)
    {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < INV_SIZE; i++)
        {
            itemStacks.set(i, list.getStack(i));
        }
        Inventories.writeNbt(nbt, itemStacks);
    }

    public static void setState(NbtCompound nbt, DevState VAL)
    {
        nbt.putString(NBT_KEY_STATE, VAL.name());
    }

    public static void setHP(NbtCompound nbt, int VAL)
    {
        nbt.putInt(NBT_KEY_HP, VAL);
    }

    public static void setOwner(NbtCompound nbt, UUID VAL)
    {
        nbt.putUuid(NBT_KEY_OWNER, VAL);
    }
}

