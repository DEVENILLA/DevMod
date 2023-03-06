package sir.dev.common.entity.dev.actions;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import sir.dev.common.entity.dev.DevEntity;

public class DevLeap
{
    private static final double speed = 1.5f;
    private static final double PullingSpeed = -2f;

    public static void execute(DevEntity dev, LivingEntity target)
    {
        if (dev == null || target == null) return;

        if (target == null) return;

        double xDir = target.getX() - dev.getX();
        double yDir = target.getY() - dev.getY();
        double zDir = target.getZ() - dev.getZ();
        double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
        Vec3d Velocity = new Vec3d
                (xDir/magnitude * speed,
                        yDir/magnitude * speed,
                        zDir/magnitude * speed);

        dev.setVelocity(Velocity);
        dev.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, target.getPos());
        dev.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
    }
}
