package sir.dev.common.entity.dev.combats;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sir.dev.common.entity.dev.DevEntity;

public class DevCombatAction
{
    public ServerWorld serverWorld;
    public World world;
    public DevEntity dev;
    public LivingEntity target;
    public LivingEntity owner;
    public ItemStack mainStack;
    public ItemStack otherStack;
    public Inventory inventory;
    public Hand handType;

    public DevCombatAction(DevEntity ent, ItemStack MainStack, ItemStack OtherStack, Hand hand)
    {
        this.dev = ent;
        this.world = dev.world;
        this.serverWorld = (ServerWorld) this.world;
        this.target = this.dev.getTarget();
        this.owner = this.dev.getOwner();
        this.mainStack = MainStack;
        this.otherStack = OtherStack;
        this.handType = hand;
        this.inventory = dev.getInventory();
    }

    public void execute()
    {
        if (dev == null || world == null || serverWorld == null || target == null || owner == null || mainStack == null || otherStack == null || handType == null || inventory == null)
        {
            return;
        }
    }



    public PersistentProjectileEntity shootArrow(int arrowStack)
    {
        int k;
        int j;
        ArrowItem arrowItem = (ArrowItem)(inventory.getStack(arrowStack).getItem() instanceof ArrowItem ? inventory.getStack(arrowStack).getItem() : Items.ARROW);
        PersistentProjectileEntity arrow = arrowItem.createArrow(world, inventory.getStack(arrowStack), dev);

        double xDir = target.getX() - dev.getX();
        double yDir = target.getBodyY(1) - arrow.getY();
        double zDir = target.getZ()  - dev.getZ();
        double g = Math.sqrt(xDir * xDir + zDir * zDir);

        arrow.setVelocity(xDir, yDir + g * (double).2f, zDir, 6,  0);

        arrow.setCritical(true);
        if ((j = EnchantmentHelper.getLevel(Enchantments.POWER, mainStack)) > 0) {
            arrow.setDamage(arrow.getDamage() + (double)j * 0.5 + 0.5);
        }
        if ((k = EnchantmentHelper.getLevel(Enchantments.PUNCH, mainStack)) > 0) {
            arrow.setPunch(k);
        }
        if (EnchantmentHelper.getLevel(Enchantments.FLAME, mainStack) > 0) {
            arrow.setOnFireFor(100);
        }

        arrow.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;

        world.spawnEntity(arrow);

        return arrow;
    }

    public void SaveInventoryData()
    {
        if (dev == null || world == null || serverWorld == null || target == null || owner == null || mainStack == null || otherStack == null || handType == null || inventory == null)
        {
            return;
        }
        if (handType == Hand.MAIN_HAND)
        {
            this.inventory.setStack(9, this.mainStack);
            this.inventory.setStack(10, this.otherStack);
        }
        else
        {
            this.inventory.setStack(10, this.mainStack);
            this.inventory.setStack(9, this.otherStack);
        }
        dev.setInventoryStacks(this.inventory);
    }
}
