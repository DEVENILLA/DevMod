package sir.dev.common.entity.dev.goals;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import org.spongepowered.include.com.google.common.collect.ImmutableSet;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DevState;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public class BreakOreGoal extends Goal {
    private final DevEntity dev;
    private BlockPos targetBlock;

    public int radius = 5;

    public BreakOreGoal(DevEntity dev, int radius) {
        this.dev = dev;
        this.radius = radius;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if ((!isHoldingPickaxe(dev) && !isHoldingAxe(dev)) || dev.getDevState() == DevState.sitting) return false;
        BlockPos devPos = dev.getBlockPos();

        targetBlock = dev.getClosestBlockPos();

        if (targetBlock != null) return true;
        return false; // no ore blocks found
    }

    public static boolean isHoldingPickaxe(DevEntity dev)
    {
        return  dev.isHolding(Items.DIAMOND_PICKAXE) ||
                dev.isHolding(Items.IRON_PICKAXE) ||
                dev.isHolding(Items.GOLDEN_PICKAXE) ||
                dev.isHolding(Items.STONE_PICKAXE) ||
                dev.isHolding(Items.WOODEN_PICKAXE) ||
                dev.isHolding(Items.NETHERITE_PICKAXE);
    }
    public static boolean isHoldingAxe(DevEntity dev)
    {
        return
                dev.isHolding(Items.DIAMOND_AXE) ||
                dev.isHolding(Items.IRON_AXE) ||
                dev.isHolding(Items.GOLDEN_AXE) ||
                dev.isHolding(Items.STONE_AXE) ||
                dev.isHolding(Items.WOODEN_AXE) ||
                dev.isHolding(Items.NETHERITE_AXE);
    }

    @Override
    public boolean shouldContinue() {
        // Keep going if there's still a target block.
        return canStart();
    }

    public void BreakBlock()
    {
        dev.triggerAnim("other", "attack");
        if (dev.ORES.contains(dev.world.getBlockState(targetBlock).getBlock()))
        {
            if (dev.isHolding(Items.DIAMOND_PICKAXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.IRON_PICKAXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.GOLDEN_PICKAXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.STONE_PICKAXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.WOODEN_PICKAXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.NETHERITE_PICKAXE, Hand.MAIN_HAND))
            {
                Inventory inv = dev.getInventory();
                ItemStack stack = dev.getMainHandStack();
                stack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });
                inv.setStack(9, stack);
                dev.setInventoryStacks(inv);
            }
            else if (dev.isHolding(Items.DIAMOND_PICKAXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.IRON_PICKAXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.GOLDEN_PICKAXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.STONE_PICKAXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.WOODEN_PICKAXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.NETHERITE_PICKAXE, Hand.OFF_HAND))
            {
                Inventory inv = dev.getInventory();
                ItemStack stack = dev.getOffHandStack();
                stack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });
                inv.setStack(10, stack);
                dev.setInventoryStacks(inv);
            }
        }
        else
        {
            if (dev.isHolding(Items.DIAMOND_AXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.IRON_AXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.GOLDEN_AXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.STONE_AXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.WOODEN_AXE, Hand.MAIN_HAND) ||
                    dev.isHolding(Items.NETHERITE_AXE, Hand.MAIN_HAND))
            {
                Inventory inv = dev.getInventory();
                ItemStack stack = dev.getMainHandStack();
                stack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });
                inv.setStack(9, stack);
                dev.setInventoryStacks(inv);
            }
            else if (dev.isHolding(Items.DIAMOND_AXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.IRON_AXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.GOLDEN_AXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.STONE_AXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.WOODEN_AXE, Hand.OFF_HAND) ||
                    dev.isHolding(Items.NETHERITE_AXE, Hand.OFF_HAND))
            {
                Inventory inv = dev.getInventory();
                ItemStack stack = dev.getOffHandStack();
                stack.damage(1, dev, new Consumer<DevEntity>() {
                    @Override
                    public void accept(DevEntity devEntity) {

                    }
                });
                inv.setStack(10, stack);
                dev.setInventoryStacks(inv);
            }
        }
        dev.world.breakBlock(targetBlock, true, dev);
    }

    @Override
    public void start() {
        // Start moving towards the target block.
        if (targetBlock == null) return;
        dev.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
    }

    @Override
    public void tick() {
        targetBlock = dev.getClosestBlockPos();

        if (targetBlock == null) {
            return;
        }

        double distance = dev.squaredDistanceTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        if (distance > 4.0) { // if we're more than 2 blocks away, move towards the block
            dev.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1);
        } else { // otherwise, break the block if we're next to it or standing above/below it
            Direction facing = Direction.DOWN; // start by looking downwards
            if (dev.isOnGround()) { // if we're on the ground, look in all directions
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = targetBlock.offset(dir);
                    if (dev.getBlockPos().isWithinDistance(neighbor, 1.0)) {
                        facing = dir;
                        break;
                    }
                }
            } else { // otherwise, look only below us
                BlockPos below = dev.getBlockPos().down();
                if (below.equals(targetBlock)) {
                    facing = Direction.DOWN;
                }
            }

            // break the block facing the correct direction
            BlockState blockState = dev.world.getBlockState(targetBlock);
            if (blockState.getHardness(dev.world, targetBlock) >= 0) { // make sure the block is breakable
                BreakBlock();
            }

            // look in the direction we broke the block
            Vector3f facingVec = facing.getUnitVector();
            float yaw = (float) Math.toDegrees(Math.atan2(facingVec.x, facingVec.z));
            float pitch = (float) Math.toDegrees(Math.atan2(facingVec.y, Math.sqrt(facingVec.x * facingVec.x + facingVec.z * facingVec.z)));
            dev.setYaw(yaw);
            dev.setPitch(pitch);
        }
    }

    @Override
    public void stop() {
        dev.getNavigation().stop();
        targetBlock = null;
        super.stop();
    }
}


