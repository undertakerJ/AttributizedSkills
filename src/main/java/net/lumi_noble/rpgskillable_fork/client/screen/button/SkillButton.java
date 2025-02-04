package net.lumi_noble.rpgskillable_fork.client.screen.button;

import net.lumi_noble.rpgskillable_fork.Config;
import net.lumi_noble.rpgskillable_fork.client.screen.SkillScreen;
import net.lumi_noble.rpgskillable_fork.common.capabilities.SkillModel;
import net.lumi_noble.rpgskillable_fork.common.network.RequestLevelUp;
import net.lumi_noble.rpgskillable_fork.common.skill.Skill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class SkillButton extends AbstractButton {
	
	private final Skill skill;
	private boolean underMaxTotal = true;

	public SkillButton(int x, int y, Skill skill) {
		super(x, y, 79, 32, Component.literal(""));
		
		this.skill = skill;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, SkillScreen.RESOURCES);
    	RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		SkillModel model = SkillModel.get();
		int level = model.getSkillLevel(skill);
		int maxLevel = Config.getMaxLevel();
		
		if (!model.underMaxTotal()) {
			underMaxTotal = false;
		}
		
		int u = ((int) Math.ceil((double)level * 4 / maxLevel) - 1) * 16 + 176;
		int v = skill.index * 16 + 128;
		
		blit(stack, x, y, 176, (level == maxLevel ? 64 : 0) + (isMouseOver(mouseX, mouseY) ? 32 : 0), width, height);
		blit(stack, x + 6, y + 8, u, v,  16, 16);
		minecraft.font.draw(stack, Component.translatable(skill.displayName), x + 22, y + 7, 0xFFFFFF);
		minecraft.font.draw(stack, level + "/" + maxLevel, x + 25, y + 18, 0xBEBEBE);
		
		if (isMouseOver(mouseX, mouseY) && level < maxLevel) {
			int cost = Config.getStartCost() + (level + 1) * Config.getCostIncrease();
			int color = minecraft.player.experienceLevel >= cost && model.underMaxTotal() ? 0x7EFC20 : 0xFC5454;
			String text = Integer.toString(cost);
			
			minecraft.font.drawShadow(stack, text, x + 73 - minecraft.font.width(text), y + 18, color);
		}
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
		if (underMaxTotal) 
		{
			RequestLevelUp.send(skill);
		}
	}
}
