package sir.dev.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sir.dev.DevMod;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
import sir.dev.common.util.IFallingBlock;

@Mixin(FallingBlockEntity.class)
public class FallingBlockMixin implements IFallingBlock {

    @Shadow private BlockState block;
    public boolean summonedByEntity = false;
    public LivingEntity summoner = null;


    @Override
    public void setInfector(LivingEntity infector) {
        if (infector != null) summonedByEntity = true;
        else summonedByEntity = false;
        this.summoner = infector;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tickInject(CallbackInfo info) {
        if (summonedByEntity && this.summoner == null) ((FallingBlockEntity)(Object)this).discard();
        if (summonedByEntity && this.summoner != null)
        {
            if (this.summoner.isDead()) ((FallingBlockEntity)(Object)this).discard();
            else
            {
                if (this.block.isAir()) {
                    ((FallingBlockEntity)(Object)this).discard();
                    return;
                }
                return;
            }
            return;
        }
        DevMod.LOGGER.info("log");
    }
}