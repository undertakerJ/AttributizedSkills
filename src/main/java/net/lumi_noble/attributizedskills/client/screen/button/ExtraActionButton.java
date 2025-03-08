package net.lumi_noble.attributizedskills.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ExtraActionButton extends AbstractButton {
    private final Component buttonText;
    private final Runnable onClickAction;
    private boolean pressed = false;
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/buttons_v2.png");
    public ExtraActionButton(int x, int y, int width, int height, Component buttonText, Runnable onClickAction) {
        super(x, y, width, height, buttonText);
        this.buttonText = buttonText;
        this.onClickAction = onClickAction;
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack stack = guiGraphics.pose();
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textureX = 176;
        int textureY;
        if (this.active) {
            if (this.isMouseOver(mouseX, mouseY)) {
                textureY = (this.pressed ? 32 : 64);
            } else {
                textureY = 0;
            }
        } else {
            textureY = 96;
        }
        guiGraphics.blit(TEXTURE, getX(), getY(), textureX, textureY, width, height);
        guiGraphics.blit(TEXTURE, getX() +6 ,getY()+8, 240, 128, 16, 16);
        int textWidth = minecraft.font.width(buttonText);
        SkillButtonV2.drawOutlinedText(guiGraphics, "Back to" , getX() + (width / 2) - (textWidth / 2) + 8, getY() + (height - minecraft.font.lineHeight) / 2 -3, 0xFFFFFF);
               SkillButtonV2.drawOutlinedText(guiGraphics, "Inventory" , getX() + (width / 2) - (textWidth / 2) + 8, getY() + (height - minecraft.font.lineHeight) / 2 + 6, 0xFFFFFF);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isMouseOver(mouseX, mouseY) && button == 0) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            this.pressed = true;
            return true;
        }
        return false;
    }

    @Override
    public void onPress() {
        if (this.onClickAction != null) {
            this.pressed = true;
            this.onClickAction.run();
        }
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.pressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
