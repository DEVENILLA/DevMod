package sir.dev.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IEntityDataSaver;

@Mixin(Entity.class)
public abstract class ModEntityDataSaverMixin implements IEntityDataSaver
{
    private NbtCompound persistentData;

    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null)
        {
            persistentData = new NbtCompound();
        }
        return persistentData;
    }

    @Override
    public void setPersistentData(NbtCompound persistentData) {
        this.persistentData = persistentData;
    }

    @Inject(at = @At("HEAD"), method = "writeNbt")
    private void writeNbtInjector(NbtCompound nbt, CallbackInfoReturnable info)
    {
        if (persistentData != null)
        {
            nbt.put(DEV_CONSTS.NBT_KEY_DATA, persistentData);
        }
    }

    @Inject(at = @At("HEAD"), method = "readNbt")
    private void readNbtInjector(NbtCompound nbt, CallbackInfo info)
    {
        if (nbt.contains(DEV_CONSTS.NBT_KEY_DATA, NbtElement.COMPOUND_TYPE))
        {
            persistentData = nbt.getCompound(DEV_CONSTS.NBT_KEY_DATA);
        }
    }
}
