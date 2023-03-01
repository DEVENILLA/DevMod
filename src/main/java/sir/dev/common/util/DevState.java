package sir.dev.common.util;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

import java.io.DataOutput;
import java.io.IOException;

public enum DevState {
    defending,
    following,
    sitting;

    public static DevState getDefault() { return DevState.following; }

    public static DevState getDevStateByIndex(int index)
    {
        if (index == defending.ordinal()) return DevState.defending;
        if (index == sitting.ordinal()) return DevState.defending;
        return getDefault();
    }
}
