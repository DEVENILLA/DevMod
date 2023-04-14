package sir.dev.client.hud.dev;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
import org.w3c.dom.html.HTMLQuoteElement;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.event.KeyInputHandler;
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
    private static final Identifier DEFEND = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/dev_defend.png");
    private static final Identifier FOLLOW = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/dev_follow.png");
    private static final Identifier SIT = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/dev_sit.png");
    private static final Identifier AIM = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/aim.png");
    private static final Identifier MENU = new Identifier(DevMod.MOD_ID, "textures/gui/hud/dev/dev_menu.png");

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

            if (client.getServer() == null) return;

            PlayerEntity player = client.getServer().getPlayerManager().getPlayer(curPlayerUUID);
            if (!player.world.isClient)
            {
                DevEntity dev = DevItem.GetDevFromPlayer(player);
                if (dev != null)
                {
                    matrixStack.scale(1, 1, 1);

                    if (dev.isMenuOpen())
                    {
                        addDevMenu(client, matrixStack, width, height);
                    }

                    switch (dev.getDevState()) {
                        case defending ->
                        {
                            added.add(DEFEND);
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
                        }
                        case following ->
                        {
                            added.add(FOLLOW);
                        }
                        case sitting ->
                        {
                            added.add(SIT);
                        }
                    }

                    for (int i = 0; i < added.size(); i++)
                    {
                        DrawSlot(added.get(i), i, matrixStack, width, height);
                    }

                    TextRenderer renderer = client.textRenderer;
                    int Xpos = (int)((width-35));
                    int Ypos = (int)((height-12));
                    renderer.draw(matrixStack, KeyInputHandler.openDevMenu.getBoundKeyLocalizedText().getString().toUpperCase(), Xpos, Ypos, 0x00FF00);
                    Xpos = (int)((width-52));
                    Ypos = (int)((height-12*4.85));
                    renderer.draw(matrixStack, String.valueOf((int)dev.getHealth()), Xpos, Ypos, 0xFF0000);
                }
            }
        }
    }


    public static void addDevMenu(MinecraftClient client, MatrixStack matrixStack, int width, int height)
    {
        TextRenderer renderer = client.textRenderer;
        int Xpos = (int)((width-35));
        int Ypos = (int)((height-12));

        RenderSystem.setShaderTexture(0, MENU);
        int slotSizeX = 120;
        int slotSizeY = 34;
        float scaleX = width / slotSizeX;
        float scaleY = height / slotSizeY;
        int i = 3;
        DrawableHelper.drawTexture
                (
                        matrixStack,
                        width-(int)(width-100/scaleX*i),
                        height/3,
                        0,
                        0,
                        (int)(width/scaleX*i),
                        (int)(height/scaleY*i),
                        (int)(width/scaleX*i),
                        (int)(height/scaleY*i)
                );

        Xpos = (int)((width-310));
        Ypos = (int)((height-12*11));
        renderer.draw(matrixStack,
                "you can call dev by pressing [" + KeyInputHandler.callDevKey.getBoundKeyLocalizedText().getString().toUpperCase() + "]",
                Xpos,
                Ypos,
                0XFFFFFF);
        Xpos = (int)((width-310));
        Ypos = (int)((height-12*9));
        renderer.draw(matrixStack,
                "you can change dev's state by pressing [" + KeyInputHandler.changeDevStateKey.getBoundKeyLocalizedText().getString().toUpperCase() + "]",
                Xpos,
                Ypos,
                0XFFFFFF);
        Xpos = (int)((width-310));
        Ypos = (int)((height-12*7));
        renderer.draw(matrixStack,
                "if you aim at an entity and click [" + KeyInputHandler.devTargetKey.getBoundKeyLocalizedText().getString().toUpperCase() + "], dev is going to target it",
                Xpos,
                Ypos,
                0XFFFFFF);
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
        int slotSizeX = 64;
        int slotSizeY = 70;
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
