package net.lumi_noble.attributizedskills.common.effects;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AttributizedSkillsEffects {
	
	public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AttributizedSkills.MOD_ID);

	public static final RegistryObject<MobEffect> ENCUMBERED = MOB_EFFECTS.register("encumbered", EffectEncumbered::new);
	public static final RegistryObject<MobEffect> ATROPHY = MOB_EFFECTS.register("atrophy", EffectAtrophy::new);


}
