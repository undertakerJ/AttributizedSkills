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
    int x = getX(), y = getY();
    Minecraft mc = Minecraft.getInstance();
    SkillModel model = SkillModel.get();
    int level       = model.getSkillLevel(skill);
    int maxLevel    = ASConfig.getMaxLevel();
    int maxTotal    = ASConfig.getMaxLevelTotal();
    int tearPoints  = model.getTearPoints();
    int totalLevel  = model.getTotalLevel();

    // определяем, можем ли вообще ещё тратить очки на *этот* скилл:
    boolean underIndividual = level < maxLevel;
    boolean underTotal      = totalLevel < maxTotal;
    boolean canLevel        = underIndividual && underTotal && !ASConfig.DISABLE_LEVEL_BUY.get();

    // если мышь не над кнопкой — сбрасываем pressed
    if (!this.isMouseOver(mouseX, mouseY)) {
      this.pressed = false;
    }

    // background
    RenderSystem.setShaderTexture(0, TEXTURE);
    RenderSystem.setShaderColor(1,1,1,1);

    int texY;
    if (!underTotal) {
      // суммарный лимит исчерпан — "серый"
      texY = 96;
    } else if (level >= maxLevel) {
      // индивидуальный лимит — "серый"
      texY = 96;
    } else if (pressed && canLevel) {
      texY = 32;
    } else if (this.isMouseOver(mouseX, mouseY) && canLevel) {
      texY = 64;
    } else {
      texY = 0;
    }

    guiGraphics.blit(TEXTURE, x, y, 176, texY, width, height);

    // icon
    int u = ((int)Math.ceil((double)level * 4 / maxLevel) - 1) * 16 + 176;
    int v = skill.index * 16 + 128;
    stack.pushPose();
    stack.scale(1.1f, 1.1f, 1);
    guiGraphics.blit(TEXTURE, (int)((x+6)/1.1), (int)((y+8)/1.1), u, v, 16, 16);
    stack.popPose();

    String skillName = Component.translatable(skill.displayName).getString();
    stack.pushPose();
    stack.scale(0.9f, 0.9f, 1);
    int nameX = (int)((x + width/2f - mc.font.width(skillName)/2f) / 0.9f) + 12;
    int nameY = (int)((y + 7)/0.9f);
    drawOutlinedText(guiGraphics, skillName, nameX, nameY, 0xFFFFFF);
    stack.popPose();

    if (this.isMouseOver(mouseX, mouseY) && canLevel) {
      int amt = Screen.hasShiftDown() ? 5 : 1;
      if (tearPoints >= amt) {
        String cost = amt == 1 ? "1 Tear" : amt + " Tears";
        int cx = x + width/2 - mc.font.width(cost)/2 + 8;
        drawOutlinedText(guiGraphics, cost, cx, y+18, 0x21F8F6);
      } else {
        int levelsToBuy = Math.min(amt, maxLevel - level);
        int costXP = 0;
        for (int i = 0; i < levelsToBuy; i++) {
          costXP += ASConfig.getStartCost() + ((level + i) - 1)*ASConfig.getCostIncrease();
        }
        boolean enoughXP   = mc.player.experienceLevel >= costXP;
        boolean staysUnder = totalLevel + levelsToBuy <= maxTotal;
        int color = (enoughXP && staysUnder) ? 0x7EFC20 : 0xFC5454;
        String cost = costXP + " levels";
        int cx = x + width/2 - mc.font.width(cost)/2 + 8;
        drawOutlinedText(guiGraphics, cost, cx, y+18, color);
      }
    } else {
      String lvlText = level + "/" + maxLevel;
      int cx = x + width/2 - mc.font.width(lvlText)/2 + 8;
      drawOutlinedText(guiGraphics, lvlText, cx, y+18, 0xBEBEBE);
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
    SkillModel model = SkillModel.get();
    int level      = model.getSkillLevel(skill);
    int maxLevel   = ASConfig.getMaxLevel();
    int maxTotal   = ASConfig.getMaxLevelTotal();
    int totalLevel = model.getTotalLevel();
    int amt        = Screen.hasShiftDown() ? 5 : 1;

    int realAmt = Math.min(amt, maxLevel - level);
    if (realAmt <= 0 || totalLevel + realAmt > maxTotal) {
      Minecraft.getInstance().player.displayClientMessage(
              Component.literal("Cannot level up any further!").withStyle(ChatFormatting.RED), true);
      return;
    }

    int tP = model.getTearPoints();
    boolean useTP = tP >= realAmt;
    int neededXP = ASConfig.getStartCost() + ((level - 1) * ASConfig.getCostIncrease());

    if (!useTP && Minecraft.getInstance().player.experienceLevel < neededXP) {
      Minecraft.getInstance().player.displayClientMessage(
              Component.literal("Not enough XP or Tear Points!").withStyle(ChatFormatting.RED), true);
      return;
    }

    ModNetworking.sendToServer(new RequestLevelUpPacket(skill, realAmt, useTP));
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
