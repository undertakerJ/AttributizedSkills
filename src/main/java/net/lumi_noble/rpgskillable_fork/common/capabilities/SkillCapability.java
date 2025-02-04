package net.lumi_noble.rpgskillable_fork.common.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class SkillCapability {
	
	public static Capability<SkillModel> SKILL_MODEL = CapabilityManager.get(new CapabilityToken<SkillModel>(){});

}
