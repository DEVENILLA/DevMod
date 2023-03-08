package sir.dev.client.hud.dev;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.networking.packets.OnDevOwnerSetsTarget;
import sir.dev.common.util.DevState;
import sir.dev.common.util.IEntityDataSaver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class DevHudOverlay implements HudRenderCallback {
    private static final Identifier MAIN_ATTACK = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/attack_main_slot.png");
    private static final Identifier OFF_ATTACK = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/attack_off_slot.png");
    private static final Identifier AIM = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/aim.png");
    private static final Identifier AI_ON = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/ai_slot.png");
    private static final Identifier AI_OFF = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/no_ai_slot.png");

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int x = 0;
        int y = 0;
        int width = 1280;
        int height = 720;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (client != null)
        {
            UUID curPlayerUUID = client.player.getUuid();
            width = client.getWindow().getScaledWidth();
            height = client.getWindow().getScaledHeight();
            ArrayList<Identifier> added = new ArrayList<Identifier>();

            PlayerEntity player = client.getServer().getPlayerManager().getPlayer(curPlayerUUID);
            if (!player.world.isClient)
            {
                DevEntity dev = DevItem.GetDevFromPlayer(player);
                if (dev != null && dev.getDevState() == DevState.defending)
                {
                    if (dev.IsDevOwnerLookin())
                    {
                        RenderSystem.setShaderTexture(0, AIM);
                        int slotSizeX = 16;
                        int slotSizeY = 16;
                        DrawableHelper.drawTexture
                                (
                                        matrixStack,
                                        width/2-slotSizeX/2,
                                        height/2-slotSizeY/2,
                                        0,
                                        0,
                                        slotSizeX,
                                        slotSizeY,
                                        slotSizeX,
                                        slotSizeY
                                );
                    }

                    if (dev.IsDevAIcontrolled())
                    {
                        added.add(AI_ON);
                    }
                    else
                    {
                        added.add(AI_OFF);
                    }

                    if (dev.getTarget() != null)
                    {
                        if (dev.getMainCooldown() <= 0 && canUse(dev, dev.getMainHandStack()))
                            added.add(MAIN_ATTACK);

                        if (dev.getOffCooldown() <= 0 && canUse(dev, dev.getOffHandStack()))
                            added.add(OFF_ATTACK);
                    }
                }
            }

            for (int i = 0; i < added.size(); i++)
            {
                DrawSlot(added.get(i), i, matrixStack, width, height);
            }
        }
    }

    public static boolean canUse(DevEntity dev, ItemStack stack)
    {
        if (stack.getItem().equals(Items.AIR))
        {
            return false;
        }

        if ((stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof BowItem) &&!dev.getInventory().containsAny(stack1 -> {
            if (stack1.getItem() instanceof ArrowItem) return true;
            return false;
        }))
        {
            return false;
        }

        return true;
    }

    public void DrawSlot(Identifier identifier, int i, MatrixStack matrixStack, int width, int height)
    {
        RenderSystem.setShaderTexture(0, identifier);
        int slotSizeX = 16;
        int slotSizeY = 32;
        int cutFromY = slotSizeY;
        int cutFromX = slotSizeX * (i+1);
        DrawableHelper.drawTexture
                (
                        matrixStack,
                        width - cutFromX,
                        height - cutFromY,
                        0,
                        0,
                        slotSizeX,
                        slotSizeY,
                        slotSizeX,
                        slotSizeY
                );
    }
}
