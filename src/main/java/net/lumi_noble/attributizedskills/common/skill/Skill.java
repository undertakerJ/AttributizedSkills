package net.lumi_noble.attributizedskills.common.skill;

public enum Skill {
	
	VITALITY(0, "skill.vitality"),
	STRENGTH(1, "skill.strength"),
	MIND(2, "skill.mind"),
	DEXTERITY(3, "skill.dexterity"),
	ENDURANCE(4, "skill.endurance"),
	INTELLIGENCE(5, "skill.intelligence");
	
	public final int index;
	public final String displayName;
	
	Skill(int index, String displayName) {
		this.index = index;
		this.displayName = displayName;
	}
}
