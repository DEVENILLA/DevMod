package sir.dev.common.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public interface IDevNavigation
{
    public void setTargetPos(BlockPos targetPos);
    public BlockPos getTargetPos();
}
