package sir.dev.common.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import sir.dev.common.block.ModBlocks;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
import sir.dev.common.entity.dev.DevEntity;
import software.bernie.example.registry.BlockEntityRegistry;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class SculkMonolithEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // We statically instantiate our RawAnimations for efficiency, consistency, and error-proofing
    private static final RawAnimation ANIMATION = RawAnimation.begin();

    public WitherEntity CurWither;

    public SculkMonolithEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_MONOLITH, pos, state);
    }

    // Let's set our animations up
    // For this one, we want it to play the "Fertilizer" animation set if it's raining,
    // or switch to a botarium if it's not.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> {
            return state.setAndContinue(ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("wither"))
        {
            if (world instanceof ServerWorld serverWorld)
            {
                CurWither = (WitherEntity) serverWorld.getEntity(nbt.getUuid("wither"));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (CurWither != null) if (CurWither.isAlive()) nbt.putUuid("wither", CurWither.getUuid());
    }

    public void tick(World world, BlockState state)
    {
        if (CurWither != null) {
            if (CurWither.isDead()) {
                CurWither = null;
                return;
            }

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, CurWither.getX(), CurWither.getY(), CurWither.getZ(), 2, .5, .5, .5, .45);
                serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, CurWither.getX(), CurWither.getY(), CurWither.getZ(), 10, .5, .5, .5, .45);
            }

            List<BlockState> blocks = new ArrayList<>();
            List<BlockPos> blockPositions = new ArrayList<>();
            if (world instanceof ServerWorld serverWorld)
            {
                for (int x = -1; x <= 1; x++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        for (int y = 0; y <= 2; y++)
                        {
                            BlockPos pos = new BlockPos(
                                    CurWither.getX()+x,
                                    CurWither.getY()+y,
                                    CurWither.getZ()+z
                            );
                            Block block = serverWorld.getBlockState(pos).getBlock();
                            if (block.getHardness() > 0)
                            {
                                blockPositions.add(pos);
                                blocks.add(serverWorld.getBlockState(pos));
                            }
                        }
                    }
                }
                for (int i = 0; i < blocks.size(); i++)
                {
                    BlockState block = blocks.get(i);
                    world.breakBlock(blockPositions.get(i), false);
                }
            }

            CurWither.setNoDrag(true);
            CurWither.setNoGravity(true);
            CurWither.endCombat();
            CurWither.noClip = true;
            CurWither.setForwardSpeed(0);
            CurWither.setMovementSpeed(0);
            CurWither.setSidewaysSpeed(0);
            CurWither.setUpwardSpeed(0);
            CurWither.setTarget(null);

            if (Random.create().nextBetween(0, 50) == 25)
            {
                switch (Random.create().nextBetween(0, 3))
                {
                    case 0 -> {
                        CurWither.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 4 ,1);
                    }
                    case 1 -> {
                        CurWither.playSound(SoundEvents.ENTITY_WARDEN_HURT, 4 ,1);
                    }
                    case 2 -> {
                        CurWither.playSound(SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, 4 ,1);
                    }
                    case 3 -> {
                        CurWither.playSound(SoundEvents.BLOCK_SCULK_FALL, 4 ,1);
                    }
                }
            }

            if (CurWither.squaredDistanceTo(getPos().toCenterPos()) > 4)
            {
                double xDir = CurWither.getX() - this.getPos().getX();
                double yDir = CurWither.getY() - this.getPos().getY();
                double zDir = CurWither.getZ() - this.getPos().getZ();
                double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                Vec3d Velocity = new Vec3d
                        (xDir/magnitude * -.1,
                                yDir/magnitude * -.1,
                                zDir/magnitude * -.1);

                CurWither.addVelocity(Velocity);
                CurWither.setVelocity(Velocity);
                CurWither.move(MovementType.SELF, Velocity);
            }
            else
            {
                if (world instanceof ServerWorld serverWorld)
                {
                    Doom(world, serverWorld);
                }
            }
        }
        else
        {
            if (Random.create().nextBetween(0, 50) == 25)
            {
                if (world instanceof ServerWorld serverWorld)
                {
                    List<WitherEntity> withers = new ArrayList<>();

                    withers = serverWorld.getEntitiesByClass(
                            WitherEntity.class,
                            Box.of(getPos().toCenterPos(), 256, world.getHeight(), 256),
                            livingEntity -> {
                                if (livingEntity == null) return false;
                                if (livingEntity.isDead()) return false;
                                return true;
                            }
                    );

                    if (withers != null && withers.size() > 0)
                    {
                        CurWither = serverWorld.getClosestEntity(withers, TargetPredicate.DEFAULT, null, getPos().getX(), getPos().getY(), getPos().getZ());
                    }
                }
            }
        }
    }

    public void Doom(World world, ServerWorld serverWorld)
    {
        serverWorld.spawnParticles(ParticleTypes.CLOUD, getPos().getX(), getPos().getY(), getPos().getZ(), 50, 2, 2, 2, .45);
        serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, getPos().getX(), getPos().getY(), getPos().getZ(), 50, 2, 2, 2, .45);
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getPos().getX(), getPos().getY(), getPos().getZ(), 50, 2, 2, 2, .45);

        serverWorld.createExplosion(null, getPos().getX(), getPos().getY(), getPos().getZ(), 12, true, World.ExplosionSourceType.BLOCK);

        AncientInfector minion = new AncientInfector(ModEntities.ANCIENT_INFECTOR, world);
        minion.setPos(getPos().getX(), getPos().getY(), getPos().getZ());
        world.spawnEntity(minion);

        CurWither.playSound(SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, 12 ,1);
        CurWither.playSound(SoundEvents.ENTITY_WARDEN_ROAR, 35 ,1);

        CurWither.discard();
        CurWither = null;
        serverWorld.breakBlock(getPos(), false);
    }
}