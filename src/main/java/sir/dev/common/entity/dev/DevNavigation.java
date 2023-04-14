package sir.dev.common.entity.dev;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sir.dev.common.util.DevState;

public class DevNavigation extends MobNavigation {
    @Nullable
    private BlockPos targetPos;
    public final DevEntity dev;

    public DevNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
        dev = (DevEntity)mobEntity;
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        this.targetPos = target;
        return super.findPathTo(target, distance);
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        this.targetPos = entity.getBlockPos();
        return super.findPathTo(entity, distance);
    }

    @Override
    public boolean startMovingTo(double x, double y, double z, double speed) {
        this.targetPos = new BlockPos(x, y, z);
        this.speed = speed;

        if (dev.isTouchingWater() || dev.isSubmergedInWater() || dev.isTargetingUnderwater())
        {
            return super.startMovingTo(x, y, z, speed);
        }

        Path path = this.findPathTo(x, y, z, 0);
        if (path != null)
        {
            return this.startMovingAlong(path, speed);
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean startMovingTo(Entity entity, double speed) {
        this.targetPos = entity.getBlockPos();
        this.speed = speed;

        if (dev.isTouchingWater() || dev.isSubmergedInWater() || dev.isTargetingUnderwater())
        {
            return super.startMovingTo(entity, speed);
        }

        Path path = this.findPathTo(entity, 0);
        if (path != null)
        {
            return this.startMovingAlong(path, speed);
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean isIdle() {
        if (dev.getDevState() == DevState.sitting) return true;
        return super.isIdle();
    }

    @Override
    public void tick() {

        if (this.isIdle()) {
            if (this.targetPos != null)
            {
                if (dev.squaredDistanceTo(targetPos.toCenterPos()) <= Math.pow(1.2, 2))
                {
                    this.targetPos = null;
                }
                else
                {
                    this.entity.getMoveControl().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), this.speed*.2f);
                }
            }
            return;
        }

        super.tick();
    }
}
