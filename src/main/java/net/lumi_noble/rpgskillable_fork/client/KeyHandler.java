package net.lumi_noble.rpgskillable_fork.client;

import net.lumi_noble.rpgskillable_fork.client.screen.SkillScreen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyHandler {
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.Key event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (event.getKey() == KeyBind.OPEN_SKILL_SCREEN.getKey().getValue() && minecraft.screen == null) {
			minecraft.setScreen(new SkillScreen());
		}
	}
}
