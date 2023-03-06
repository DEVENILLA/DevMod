package sir.dev.common.entity.dev.goals;/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DevState;

public class DevSwimAroundGoal
        extends WanderAroundGoal {
    public DevSwimAroundGoal(PathAwareEntity pathAwareEntity, double d, int i) {
        super(pathAwareEntity, d, i);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        return LookTargetUtil.find(this.mob, 10, 7);
    }

    @Override
    public boolean canStart() {
        DevEntity dev = (DevEntity) this.mob;
        if (dev.getOwner() == null) return false;
        if (dev.getDevState() == DevState.sitting) return false;
        if (dev.IsDevCalled() == true) return false;
        return super.canStart();
    }
}

