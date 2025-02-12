package net.lumi_noble.attributizedskills.client;

import org.lwjgl.glfw.GLFW;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = AttributizedSkills.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBind {

	public final static KeyMapping OPEN_SKILL_SCREEN = new KeyMapping("key.attributizedskills.open_skill_screen", GLFW.GLFW_KEY_EQUAL, "key.category.attributizedskills.general");
	
	@SubscribeEvent
	public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
		event.register(OPEN_SKILL_SCREEN);
	}
}
