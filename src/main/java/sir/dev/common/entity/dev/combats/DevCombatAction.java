package sir.dev.common.entity.dev.combats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
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
    }

    public void execute()
    {
        if (dev == null || world == null || serverWorld == null || target == null || owner == null || mainStack == null || otherStack == null || handType == null)
        {
            return;
        }
    }
}
