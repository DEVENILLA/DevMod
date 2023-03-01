package sir.dev.common.util;

import net.minecraft.nbt.NbtCompound;

public interface IEntityDataSaver
{
    public NbtCompound getPersistentData();
    public void setPersistentData(NbtCompound compound);
}
