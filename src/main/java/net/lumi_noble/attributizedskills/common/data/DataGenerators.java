package net.lumi_noble.attributizedskills.common.data;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		
		DataGenerator generator = event.getGenerator();
		
		generator.addProvider(event.includeServer(), new AttributizedSkillsDungeonLoot(event.getGenerator().getPackOutput(), AttributizedSkills.MOD_ID));
		generator.addProvider(event.includeClient(), new AttributizedSkillsLang(event.getGenerator().getPackOutput(), "en_us"));
	}
}
