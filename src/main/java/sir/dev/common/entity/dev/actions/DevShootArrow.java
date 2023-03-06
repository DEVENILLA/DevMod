package sir.dev.common.entity.dev.actions;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.Vec3d;
import sir.dev.common.entity.dev.DevEntity;

public class DevShootArrow
{
    private static final double speed = 10f;
    private static final double shootDistance = 2f;

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

        Vec3d shootPosAdd = new Vec3d
                (xDir/magnitude * shootDistance,
                        yDir/magnitude * shootDistance,
                        zDir/magnitude * shootDistance);

        ArrowEntity arrowEntity = new ArrowEntity(dev.world, (LivingEntity)dev);
        arrowEntity.setPosition(dev.getX() + shootPosAdd.x, dev.getY() + shootPosAdd.y, dev.getZ() + shootPosAdd.z);
        arrowEntity.setPierceLevel((byte)3);
        arrowEntity.lookAt(arrowEntity.getCommandSource().getEntityAnchor(), target.getPos());
        arrowEntity.setCritical(true);
        arrowEntity.speed = 40;
        arrowEntity.horizontalSpeed = 40;
        dev.world.spawnEntity(arrowEntity);
    }
}
