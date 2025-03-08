package net.lumi_noble.attributizedskills.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class StatIconButton extends Button {

    public StatIconButton(int x, int y, int width, int height, Component text, OnPress onPress, CreateNarration pCreateNarration) {
        super(x, y, width, height, text, onPress, pCreateNarration);
    }


    private static final ResourceLocation STATS_ICON  = new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/skill_icon_color.png");
    private static final ResourceLocation STATS_ICON_NO_COLOR  = new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/skill_icon_no_color.png");



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.screen;
        PoseStack poseStack = guiGraphics.pose();
        if (currentScreen instanceof InventoryScreen) {
            int newX = ((EffectRenderingInventoryScreen<?>) currentScreen).getGuiLeft() + 77;
            int newY = ((EffectRenderingInventoryScreen<?>) currentScreen).getGuiTop() + 44;
            this.setX(newX);
            this.setY(newY);
            this.active = true;
            this.visible = true;
        } else if (currentScreen instanceof CreativeModeInventoryScreen creativeScreen) {
            if (creativeScreen.isInventoryOpen()) {
                int newX = creativeScreen.getGuiLeft() + 125;
                int newY = creativeScreen.getGuiTop() + 20;
                this.setX(newX);
                this.setY(newY);
                this.active = true;
                this.visible = true;
            } else {
                this.active = false;
                this.visible = false;
            }
        } else if (currentScreen instanceof CuriosScreen curiosScreen) {
            int newX = curiosScreen.getGuiLeft() + 77;
            int newY = curiosScreen.getGuiTop() + 44;
            this.setX(newX);
            this.setY(newY);
            this.active = true;
            this.visible = true;
        }

        ResourceLocation texture = this.isMouseOver(mouseX, mouseY) ? STATS_ICON : STATS_ICON_NO_COLOR;
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.pushPose();
        guiGraphics.blit(texture, getX(), getY(), 0, 0, this.width, this.height, this.width, this.height);
        poseStack.popPose();
    }
}
