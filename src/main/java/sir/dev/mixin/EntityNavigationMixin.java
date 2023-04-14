package sir.dev.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IDevNavigation;
import sir.dev.common.util.IEntityDataSaver;

@Mixin(EntityNavigation.class)
public abstract class EntityNavigationMixin implements IDevNavigation
{
    @Shadow private @Nullable BlockPos currentTarget;

    @Shadow @Final protected MobEntity entity;

    @Override
    public BlockPos getTargetPos() {
        return this.currentTarget;
    }

    @Override
    public void setTargetPos(BlockPos targetPos) {
        this.currentTarget = targetPos;
    }

    @Inject(at = @At("HEAD"), method = "stop")
    private void injectStop(CallbackInfo info) {
        //if (entity.getMoveControl() != null)
        //{
        //    if (entity.getMoveControl() instanceof DevEntity.DevMoveControl devMoveControl)
        //    {
        //        devMoveControl.resetPos();
        //    }
        //}
    }
}
