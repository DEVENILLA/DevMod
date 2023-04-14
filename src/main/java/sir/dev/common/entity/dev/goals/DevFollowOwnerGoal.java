package sir.dev.common.entity.dev.goals;

import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DevState;

public class DevFollowOwnerGoal extends Goal {
    private final TameableEntity tameable;
    private LivingEntity owner;
    private final WorldView world;
    private final double speed;
    private Path path;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final float maxDistance;
    private final float minDistance;
    private final boolean leavesAllowed;
    private final boolean activatesWhenCalled;
    private final boolean activatesWhenHasTarget;

    public DevFollowOwnerGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance, boolean activatesWhenHasTarget, boolean activatesWhenCalled, boolean leavesAllowed) {
        this.tameable = tameable;
        this.world = tameable.world;
        this.speed = speed;
        this.navigation = tameable.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = leavesAllowed;
        this.activatesWhenCalled = activatesWhenCalled;
        this.activatesWhenHasTarget = activatesWhenHasTarget;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        if (!(tameable.getNavigation() instanceof MobNavigation) && !(tameable.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canStart() {
        long l = this.tameable.world.getTime();
        LivingEntity livingEntity = this.tameable.getOwner();
        DevEntity dev = (DevEntity) this.tameable;

        if (activatesWhenCalled != dev.IsDevCalled()) return false;
        if ((dev.getTarget() != null) != activatesWhenHasTarget) return false;

        if (dev.getDevState() == DevState.sitting)
        {
            return false;
        }

        if (livingEntity == null) {
            return false;
        }
        if (livingEntity.isDead()) {
            return false;
        }
        if (livingEntity.isSpectator()) {
            return false;
        }
        if (this.tameable.isSitting()) {
            return false;
        }
        if (this.tameable.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
            return false;
        }
        this.path = this.tameable.getNavigation().findPathTo(livingEntity, 0);
        if (this.path == null) {
            return false;
        }
        this.owner = livingEntity;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
        }
        if (this.tameable.isSitting()) {
            return false;
        }
        return !(this.tameable.squaredDistanceTo(this.owner) <= (double)(this.maxDistance * this.maxDistance));
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.tameable.getNavigation().startMovingAlong(this.path, this.speed);
        DevEntity dev = (DevEntity) this.tameable;
        this.updateCountdownTicks = 0;
        if (dev.getUnderwaterTarget() == null)
            dev.setUnderwaterTarget(this.owner);
        //this.oldWaterPathfindingPenalty = this.tameable.getPathfindingPenalty(PathNodeType.WATER);
        //this.tameable.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        DevEntity dev = (DevEntity) this.tameable;
        if (dev.getOwner() != null)
        {
            if (dev.getUnderwaterTarget() == dev.getOwner())
                dev.setUnderwaterTarget(null);
        }
        this.navigation.stop();
        //this.tameable.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStop() {
        long l = this.tameable.world.getTime();
        LivingEntity livingEntity = this.tameable.getOwner();
        DevEntity dev = (DevEntity) this.tameable;

        if (dev.getDevState() == DevState.sitting)
        {
            return true;
        }

        if (livingEntity == null) {
            return true;
        }
        if (livingEntity.isDead()) {
            return true;
        }
        if (livingEntity.isSpectator()) {
            return true;
        }
        if (this.tameable.isSitting()) {
            return true;
        }
        if (this.tameable.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
            return true;
        }
        this.path = this.tameable.getNavigation().findPathTo(livingEntity, 0);
        if (this.path == null) {
            return true;
        }
        this.owner = livingEntity;
        return false;
    }

    @Override
    public void tick() {
        this.tameable.getLookControl().lookAt(this.owner, 30.0f, 30.0f);
        DevEntity dev = (DevEntity) this.tameable;
        if (dev.getUnderwaterTarget() == null)
            dev.setUnderwaterTarget(this.owner);
        this.path = this.tameable.getNavigation().findPathTo(this.owner, 0);

        if (this.tameable.squaredDistanceTo(this.owner) >= Math.pow(DevEntity.CHASE_DISTANCE, 2)) {
            this.tryTeleport();
        }
        else
        {
            this.tameable.getNavigation().startMovingTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(), this.speed);
        }
    }

    private void tryTeleport() {
        BlockPos blockPos = this.owner.getBlockPos();
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
        if (Math.abs((double)x - this.owner.getX()) < 2.0 && Math.abs((double)z - this.owner.getZ()) < 2.0) {
            return false;
        }
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.tameable.refreshPositionAndAngles((double)x + 0.5, y, (double)z + 0.5, this.tameable.getYaw(), this.tameable.getPitch());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy());
        if (pathNodeType != PathNodeType.WALKABLE && pathNodeType != PathNodeType.WATER) {
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

