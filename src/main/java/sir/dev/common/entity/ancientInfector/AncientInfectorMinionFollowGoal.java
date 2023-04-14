package sir.dev.common.entity.ancientInfector;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class AncientInfectorMinionFollowGoal extends Goal {
    public static final int TELEPORT_DISTANCE = 12;
    private static final int HORIZONTAL_RANGE = 2;
    private static final int HORIZONTAL_VARIATION = 3;
    private static final int VERTICAL_VARIATION = 1;
    private final AncientInfectorMinion tameable;
    private final WorldView world;
    private final double speed;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private final boolean leavesAllowed;

    public AncientInfectorMinionFollowGoal(AncientInfectorMinion minion, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
        this.tameable = minion;
        this.world = minion.world;
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = true;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.tameable.Owner;
        if (livingEntity == null) {
            return false;
        }
        if (livingEntity.isSpectator()) {
            return false;
        }
        if (this.tameable.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (tameable.getNavigation().isIdle()) {
            return false;
        }
        return !(this.tameable.squaredDistanceTo(this.tameable.Owner) <= (double) (this.maxDistance * this.maxDistance));
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override
    public void stop() {
        tameable.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.tameable.getLookControl().lookAt(this.tameable.Owner, 10.0f, this.tameable.getMaxLookPitchChange());
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.getTickCount(10);
        if (this.tameable.isLeashed() || this.tameable.hasVehicle()) {
            return;
        }
        if (this.tameable.squaredDistanceTo(this.tameable.Owner) >= 144.0) {
            this.tryTeleport();
        } else {
            tameable.getNavigation().startMovingTo(this.tameable.Owner, this.speed);
        }
    }

    private void tryTeleport() {
        BlockPos blockPos = this.tameable.Owner.getBlockPos();
        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);
            boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (!bl) continue;
            return;
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.tameable.Owner.getX()) < 2.0 && Math.abs((double) z - this.tameable.Owner.getZ()) < 2.0) {
            return false;
        }
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.tameable.refreshPositionAndAngles((double) x + 0.5, y, (double) z + 0.5, this.tameable.getYaw(), this.tameable.getPitch());
        tameable.getNavigation().stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy());
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        }
        BlockState blockState = this.world.getBlockState(pos.down());
        if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos blockPos = pos.subtract(this.tameable.getBlockPos());
        return this.world.isSpaceEmpty(this.tameable, this.tameable.getBoundingBox().offset(blockPos));
    }

    private int getRandomInt(int min, int max) {
        return this.tameable.getRandom().nextInt(max - min + 1) + min;
    }
}