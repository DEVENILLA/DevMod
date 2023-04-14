package sir.dev.common.entity.ancientInfector;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class UltimateInfectorMinionFollowGoal extends Goal {
    
    public UltimateInfectorMinion minion;
    
    public UltimateInfectorMinionFollowGoal(UltimateInfectorMinion minion1) {
        minion = minion1;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = minion.getTarget();
        if (livingEntity != null && livingEntity.isAlive() && !minion.getMoveControl().isMoving() && minion.getRandom().nextInt(UltimateInfectorMinionFollowGoal.toGoalTicks(7)) == 0) {
            return minion.squaredDistanceTo(livingEntity) > 4.0;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return minion.getMoveControl().isMoving() && minion.isCharging() && minion.getTarget() != null && minion.getTarget().isAlive();
    }

    @Override
    public void start() {
        LivingEntity livingEntity = minion.getTarget();
        if (livingEntity != null) {
            Vec3d vec3d = livingEntity.getEyePos();
            minion.getMoveControl().moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
        }
        minion.setCharging(true);
        minion.playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0f, 1.0f);
    }

    @Override
    public void stop() {
        minion.setCharging(false);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = minion.getTarget();
        if (livingEntity == null) {
            return;
        }
        if (minion.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
            minion.tryAttack(livingEntity);
            minion.setCharging(false);
        } else {
            Vec3d vec3d = livingEntity.getEyePos();
            minion.getMoveControl().moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
        }
    }
}