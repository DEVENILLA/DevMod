package sir.dev.common.entity.dev.goals;/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */


import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DevState;

public class DevLookAroundGoal
        extends Goal {
    private final MobEntity mob;
    private double deltaX;
    private double deltaZ;
    private int lookTime;

    public DevLookAroundGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        DevEntity dev = (DevEntity) this.mob;
        if (dev.getOwner() == null) return false;
        if (dev.getDevState() == DevState.sitting) return false;
        if (dev.IsDevCalled() == true) return false;
        return this.mob.getRandom().nextFloat() < 0.02f;
    }

    @Override
    public boolean shouldContinue() {
        return this.lookTime >= 0;
    }

    @Override
    public void start() {
        double d = Math.PI * 2 * this.mob.getRandom().nextDouble();
        this.deltaX = Math.cos(d);
        this.deltaZ = Math.sin(d);
        this.lookTime = 20 + this.mob.getRandom().nextInt(20);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        --this.lookTime;
        this.mob.getLookControl().lookAt(this.mob.getX() + this.deltaX, this.mob.getEyeY(), this.mob.getZ() + this.deltaZ);
    }
}

