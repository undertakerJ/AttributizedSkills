package net.lumi_noble.attributizedskills.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.lumi_noble.attributizedskills.common.network.packets.RequestLevelUpPacket;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

  private static final ResourceLocation TEXTURE = new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/buttons_v2.png");

  public SkillButtonV2(int x, int y, Skill skill) {
    super(x, y, 79, 32, Component.literal(""));

    this.skill = skill;
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    PoseStack stack = guiGraphics.pose();
    int x = getX();
    int y = getY();
    if (!this.isMouseOver(mouseX, mouseY)) {
      this.pressed = false;
    }
    Minecraft minecraft = Minecraft.getInstance();
    RenderSystem.setShaderTexture(
        0, TEXTURE);
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

    SkillModel model = SkillModel.get();
    int level = model.getSkillLevel(skill);
    int maxLevel = ASConfig.getMaxLevel();
    int tearPoints = model.getTearPoints();

    if (!model.underMaxTotal()) {
      underMaxTotal = false;
    }

    int textureX = 176;

    int textureY = 0;
    if (level == maxLevel) {
      textureY = 96;
    } else if (pressed && !ASConfig.DISABLE_LEVEL_BUY.get()) {
      textureY = 32;
    } else if (this.isMouseOver(mouseX, mouseY)) {
      textureY = 64;
    } else {
      textureY = 0;
    }

    guiGraphics.blit(
            TEXTURE,
        x,
        y,
        textureX,
        textureY,
        width,
        height);

    int u = ((int) Math.ceil((double) level * 4 / maxLevel) - 1) * 16 + 176;
    int v = skill.index * 16 + 128;
    stack.pushPose();
    float scale1 = 1.1f;
    stack.scale(scale1, scale1, scale1);
    guiGraphics.blit(
            TEXTURE,
        (int) ((x + 6) / scale1),
        (int) ((y + 8) / scale1),
        u,
        v,
        16,
        16);
    stack.popPose();

    String skillName = Component.translatable(skill.displayName).getString();
    int skillTextWidth = minecraft.font.width(skillName);
    int centeredSkillX = x + (width / 2) - (skillTextWidth / 2);

    float scale = 0.90F;

    stack.pushPose();
    stack.scale(scale, scale, scale);
    drawOutlinedText(
        guiGraphics,
        skillName,
        (int) (centeredSkillX / scale) + 12,
        (int) ((y + 7) / scale),
        0xFFFFFF);
    stack.popPose();
    String levelText = level + "/" + maxLevel;
    int levelTextWidth = minecraft.font.width(levelText);
    int centeredLevelX = x + (width / 2) - (levelTextWidth / 2);
    if (this.isMouseOver(mouseX, mouseY) && level < maxLevel && !ASConfig.DISABLE_LEVEL_BUY.get()) {
      int numLevels = Screen.hasShiftDown() ? 5 : 1;

      if (tearPoints >= numLevels) {
        String costText = numLevels == 1 ? "1 Tear" : numLevels + " Tears";
        int costColor = 0x21F8F6;
        int costTextWidth = minecraft.font.width(costText);
        int centeredCostX = x + (width / 2) - (costTextWidth / 2);
        drawOutlinedText(guiGraphics, costText, centeredCostX + 8, y + 18, costColor);
      } else {
        int totalCost = 0;
        for (int i = 0; i < numLevels && (level + i) < maxLevel; i++) {
          totalCost += ASConfig.getStartCost() + ((level + i) - 1) * ASConfig.getCostIncrease();
        }
        int costColor =
            (minecraft.player.experienceLevel >= totalCost && model.underMaxTotal())
                ? 0x7EFC20
                : 0xFC5454;
        String costText = totalCost + " levels";
        int costTextWidth = minecraft.font.width(costText);
        int centeredCostX = x + (width / 2) - (costTextWidth / 2);
        drawOutlinedText(guiGraphics, costText, centeredCostX + 8, y + 18, costColor);
      }
    } else {
      drawOutlinedText(guiGraphics, levelText, centeredLevelX + 8, y + 18, 0xBEBEBE);
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
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

  @Override
  public void onPress() {
    if (underMaxTotal && !ASConfig.DISABLE_LEVEL_BUY.get()) {
      SkillModel model = SkillModel.get();
      int maxPointsToUse = Screen.hasShiftDown() ? 5 : 1;
      int tearPoints = model.getTearPoints();
      int playerXP = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.experienceLevel : 0;

      boolean useTearPoints = (tearPoints >= maxPointsToUse);
      int requiredXP = ASConfig.getStartCost() + ((model.getSkillLevel(skill) - 1) * ASConfig.getCostIncrease());
      
      if (useTearPoints || playerXP >= requiredXP) {
        ModNetworking.sendToServer(new RequestLevelUpPacket(skill, maxPointsToUse, useTearPoints));
      } else {
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Not enough XP or Tear Points!").withStyle(ChatFormatting.RED), true);
      }
    }
  }


  public static void drawOutlinedText(
      GuiGraphics guiGraphics, String text, int x, int y, int color) {
    Font font = Minecraft.getInstance().font;
    guiGraphics.drawString(font, text, x + 1, y, 0);
    guiGraphics.drawString(font, text, x - 1, y, 0);
    guiGraphics.drawString(font, text, x, y + 1, 0);
    guiGraphics.drawString(font, text, x, y - 1, 0);
    guiGraphics.drawString(font, text, x, y, color);
  }
}
