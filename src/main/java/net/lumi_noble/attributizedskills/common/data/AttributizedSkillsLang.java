package net.lumi_noble.attributizedskills.common.data;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.effects.AttributizedSkillsEffects;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class AttributizedSkillsLang extends LanguageProvider  {

	public AttributizedSkillsLang(PackOutput gen, String locale) {
		super(gen, AttributizedSkills.MOD_ID, locale);
	}

	@Override
	protected void addTranslations() {

		// item
		add(AttributizedSkills.LARVAL_TEAR.get(), "Larval Tear");
		add(AttributizedSkills.TEAR_OF_THE_GODDESS.get(), "Tear of the Goddess");
		add("attributizedskills.tooltip.larval_tear_reset", "Resets all skills on use. Returns xp levels equal to the total amount of levels one had");
		add("attributizedskills.tooltip.larval_tear_loot", "Converts to 12 bottles of experience on use.");
		add("attributizedskills.tooltip.larval_tear_off", "Just a worthless rock... How pity...");
		add("larvar_tear.reset_successful", "Your skills began to fade away... Skill issue, dear...");
		add("tear_of_the_goddess.add", "You gain 1 Tear Point.");

		// effects
		add(AttributizedSkillsEffects.ATROPHY.get(), "Atrophy");
		add(AttributizedSkillsEffects.ENCUMBERED.get(), "Encumbered");
		// Commands
		add("message.command_get", "has next skill and level:");
		add("command.set_skill_level", "Set %1$s level to %2$s for player %3$s");
		add("command.remove_bonus.success", "Removed bonus for %1$s from %2$s");
		add("command.remove_bonus.failure", "No bonus for attribute %1$s found for %2$s");
		add("command.remove_requirements.success", "Removed all requirements for item %1$s");
		add("command.remove_requirements.failure.no_item", "You need to hold item in hand!");
		add("command.add_requirements.failure.wrong_format", "Wrong format! Example: vitality 20 strength 50");
		add( "command.add_requirements.failure.wrong_format_number", "Wrong number for skill: %1$s. Level cannot be less than 1.");
		add("command.add_requirements.failure.wrong_number", "Wrong number for skill: %1$s.");
		add("command.add_requirements.failure.wrong_skill", "Wrong skill: %1$s. Correct skills: VITALITY, STRENGTH, MIND, DEXTERITY, ENDURANCE, INTELLIGENCE");
		add("command.add_bonus.failure.exist", "Bonus for attribute %1$s already exists for %2$s");
    	add("command.add_bonus.success", "Added bonus for %1$s: %2$s");
		add("command.error.remove_all_bonuses", "If you see this message, then dev of mod have a severe case of skill issue, and was unable to remove bonuses on live players.");
		add("command.set_tear_point.success", "Set %1$s free tear points to %2$s");
		//Iron's Spells compat
		add("spell.requirement.not_met", "You cannot cast %1$s. Missing requirement: %2$s (%3$s)");
		add("command.add_spell_req.success", "Set requirements for spell: %1$s");
		add("command.remove_spell_req.error", "There no requirements for spell: %1$s");
		add("command.remove_spell_req.success", "Removed requirements for spell: %1$s");
		add("tooltip.spell.requirements", "Spell Requirements");
		add("tooltip.item.requirements", "Item Requirements");
		add("tooltip.enchant.requirements", "Enchantment Requirements");
		//Apotheosis Compat
		add("command.add_apoth_req.success", "Set requirements for rarity: %1$s");
		add("command.remove_apoth_req.error", "There no requirements for rarity: %1$s");
		add("command.remove_apoth_req.success", "Removed requirements for rarity: %1$s");

		// Attributes
		add("attribute.attributizedskills.vitality", "Vitality");
		add("attribute.attributizedskills.dexterity", "Dexterity");
		add("attribute.attributizedskills.strength", "Strength");
		add("attribute.attributizedskills.mind", "Mind");
		add("attribute.attributizedskills.endurance", "Endurance");
		add("attribute.attributizedskills.intelligence", "Intelligence");

		// UI
		add("gui.total_level", "Total Level: ");
		add("container.skills", "Skills");
		add("tooltip.requirements", "Requirements");
		add("key.attributizedskills.open_skill_screen", "Open Skills");
		add("key.category.attributizedskills.general", "Attributized Skills");
		add("overlay.main_hand_message", "Your right arm struggles to wield the item");
		add("overlay.off_hand_message", "Your left arm struggles to wield the item");
		add("overlay.default_message", "You lack the skills to use this!");
		add("ui.skills.limit", "Skill limit: %1$s");
		add("ui.skills.hold_shift", "Hold SHIFT to level up skill 5 times.");
		// skills
		add("skill.vitality", "Vitality");
		add("skill.endurance", "Endurance");
		add("skill.strength", "Strength");
		add("skill.mind", "Mind");
		add("skill.dexterity", "Dexterity");
		add("skill.intelligence", "Intelligence");
		
		// JEED
		add("effect.attributizedskills.atrophy.description", "Severely reduces attack speed, damage, and spell damage.");
		add("effect.attributizedskills.encumbered.description", "Severely reduces movespeed and prevents jumping. Drains feathers.");
	}

}
