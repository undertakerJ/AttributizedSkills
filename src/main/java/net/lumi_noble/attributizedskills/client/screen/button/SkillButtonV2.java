package net.lumi_noble.attributizedskills.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.lumi_noble.attributizedskills.common.network.packets.RequestLevelUpPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class SkillButtonV2 extends AbstractButton {

    private final Skill skill;
    private boolean underMaxTotal = true;
    private boolean pressed = false;

    public SkillButtonV2(int x, int y, Skill skill) {
        super(x, y, 79, 32, Component.literal(""));

        this.skill = skill;
    }
    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            this.pressed = false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/buttons_v2.png"));
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        SkillModel model = SkillModel.get();
        int level = model.getSkillLevel(skill);
        int maxLevel = Config.getMaxLevel();

        if (!model.underMaxTotal()) {
            underMaxTotal = false;
        }

        int textureX = 176;


        int textureY = 0;
        if (level == maxLevel) {
            textureY = 96;
        } else if (pressed && !Config.DISABLE_LEVEL_BUY.get()) {
            textureY = 32;
        } else if (this.isMouseOver(mouseX, mouseY)) {
            textureY = 64;
        } else {
            textureY = 0;
        }

        blit(stack, x, y, textureX, textureY, width, height);

        int u = ((int) Math.ceil((double)level * 4 / maxLevel) - 1) * 16 + 176;
        int v = skill.index * 16 + 128;
        stack.pushPose();
        float scale1 = 1.1f;
        stack.scale(scale1, scale1, scale1);
        blit(stack, (int) ((x + 6) /scale1), (int) ((y + 8) / scale1), u, v, 16, 16);
        stack.popPose();


        String skillName = Component.translatable(skill.displayName).getString();
        int skillTextWidth = minecraft.font.width(skillName);
        int centeredSkillX = x + (width/2) - (skillTextWidth / 2);

        float scale = 0.90F;

        stack.pushPose();
        stack.scale(scale, scale, scale);
        drawOutlinedText(stack, skillName, (int) (centeredSkillX / scale) + 12, (int) ((y + 7) / scale), 0xFFFFFF);
        stack.popPose();
        String levelText = level + "/" + maxLevel;
        int levelTextWidth = minecraft.font.width(levelText);
        int centeredLevelX = x + (width / 2) - (levelTextWidth / 2);
        if (this.isMouseOver(mouseX, mouseY) && level < maxLevel && !Config.DISABLE_LEVEL_BUY.get()) {
            int numLevels = Screen.hasShiftDown() ? 5 : 1;
            int totalCost = 0;
            for (int i = 0; i < numLevels && (level + i) < maxLevel; i++) {
                totalCost += Config.getStartCost() + ((level + i) - 1) * Config.getCostIncrease();
            }
            int costColor = (minecraft.player.experienceLevel >= totalCost && model.underMaxTotal()) ? 0x7EFC20 : 0xFC5454;
            String costText = totalCost + " levels";
            int costTextWidth = minecraft.font.width(costText);
            int centeredCostX = x + (width / 2) - (costTextWidth / 2);
            drawOutlinedText(stack, costText, centeredCostX + 8, y + 18, costColor);
        } else {
            drawOutlinedText(stack, levelText, centeredLevelX + 8, y + 18, 0xBEBEBE);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isMouseOver(mouseX, mouseY)) {
            if (button == 1) {
                return false;
            }
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.pressed = true;
        onPress();
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.pressed = false;
        this.onRelease(pMouseX, pMouseY);
        return true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.pressed = false;
    }


    @Override
    public void playDownSound(SoundManager manager) {
        if (underMaxTotal) {
            manager.play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.8F, 0.4F));
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }

    @Override
    public void onPress() {
        if (underMaxTotal && !Config.DISABLE_LEVEL_BUY.get())
        {
            if (Screen.hasShiftDown()) {
                ModNetworking.sendToServer(new RequestLevelUpPacket(skill, 5));
            } else {
                ModNetworking.sendToServer(new RequestLevelUpPacket(skill, 1));
            }
        }
    }

    public static void drawOutlinedText(PoseStack poseStack, String text, int x, int y, int color) {
        Font font = Minecraft.getInstance().font;
        GuiComponent.drawString(poseStack, font, text, x + 1, y, 0);
        GuiComponent.drawString(poseStack, font, text, x - 1, y, 0);
        GuiComponent.drawString(poseStack, font, text, x, y + 1, 0);
        GuiComponent.drawString(poseStack, font, text, x, y - 1, 0);
        GuiComponent.drawString(poseStack, font, text, x, y, color);
    }
}