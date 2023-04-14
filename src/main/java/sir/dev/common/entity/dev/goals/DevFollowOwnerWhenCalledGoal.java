package sir.dev.common.entity.dev.goals;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DevState;

import java.util.EnumSet;

public class DevFollowOwnerWhenCalledGoal extends Goal {
    public static final int TELEPORT_DISTANCE = 12;
    private static final int HORIZONTAL_RANGE = 2;
    private static final int HORIZONTAL_VARIATION = 3;
    private static final int VERTICAL_VARIATION = 1;
    private final TameableEntity tameable;
    private LivingEntity owner;
    private final WorldView world;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;
    private Path path;
    private final boolean leavesAllowed;
    private final boolean activatesWhenCalled;

    public DevFollowOwnerWhenCalledGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance, boolean activatesWhenCalled, boolean leavesAllowed) {
        this.tameable = tameable;
        this.world = tameable.world;
        this.speed = speed;
        this.navigation = tameable.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = leavesAllowed;
        this.activatesWhenCalled = activatesWhenCalled;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(tameable.getNavigation() instanceof MobNavigation) && !(tameable.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.tameable.getOwner();
        DevEntity dev = (DevEntity) this.tameable;

        if (activatesWhenCalled != dev.IsDevCalled()) return false;

        if (dev.getDevState() == DevState.sitting)
        {
            return false;
        }

        if (livingEntity == null) {
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
        this.owner = livingEntity;
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
        DevEntity dev = (DevEntity) this.tameable;
        dev.setTarget(null);
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
    public void tick() {
        this.tameable.getLookControl().lookAt(this.owner, 30.0f, 30.0f);
        DevEntity dev = (DevEntity) this.tameable;
        dev.setUnderwaterTarget(this.owner);

        if (this.tameable.squaredDistanceTo(this.owner) >= Math.pow(DevEntity.TARGET_DISTANCE, 2)) {
            this.tryTeleport();
        } else {
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

        LightningEntity lighting = new LightningEntity(EntityType.LIGHTNING_BOLT, tameable.world);
        lighting.setPos(tameable.getX(), tameable.getY(), tameable.getZ());
        lighting.setCosmetic(true);
        lighting.setOnFire(false);
        tameable.world.spawnEntity(lighting);
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

