package sir.dev.client.screen.dev;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;

public class Damaged3DevScreen extends HandledScreen<Damaged3DevScreenHandler>
{
    public int BgWidth = 176;
    public int BgHeight = 166+16+16;

    @Override
    public boolean shouldPause() {
        return true;
    }

    public Damaged3DevScreen(Damaged3DevScreenHandler handler, PlayerInventory inventory, Text title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, new Identifier(DevMod.MOD_ID, "textures/gui/container/dev_inventory_dmg_3.png"));
        int i = (this.width - BgWidth) / 2;
        int j = (this.height - BgHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, BgWidth, BgHeight);
    }
}
