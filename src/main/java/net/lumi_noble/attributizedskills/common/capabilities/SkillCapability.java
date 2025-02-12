package net.lumi_noble.attributizedskills.common.capabilities;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class SkillCapability {
	
	public static Capability<SkillModel> SKILL_MODEL = CapabilityManager.get(new CapabilityToken<SkillModel>() {
	} );
	public static final ResourceLocation KEY = new ResourceLocation(AttributizedSkills.MOD_ID, "skill_model");

}
