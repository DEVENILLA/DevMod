package sir.dev.common.entity.ancientInfector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AncientInfectorMinionNavigation extends MobNavigation {
    @Nullable
    private BlockPos targetPos;
    public final AncientInfectorMinion infector;

    public AncientInfectorMinionNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
        infector = (AncientInfectorMinion)mobEntity;
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
    public boolean startMovingTo(Entity entity, double speed) {
        this.targetPos = entity.getBlockPos();
        this.speed = speed;

        if (infector.isTouchingWater() || infector.isSubmergedInWater() || infector.isTargetingUnderwater())
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
        return super.isIdle();
    }

    @Override
    public void tick() {

        if (this.isIdle()) {
            if (this.targetPos != null)
            {
                if (infector.squaredDistanceTo(targetPos.toCenterPos()) <= Math.pow(1.2, 2))
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
