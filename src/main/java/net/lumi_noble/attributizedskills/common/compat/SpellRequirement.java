package net.lumi_noble.attributizedskills.common.compat;

import net.lumi_noble.attributizedskills.common.skill.Skill;

import java.util.HashMap;
import java.util.Map;

public class SpellRequirement {
    private final Map<Skill, Integer> baseRequirements;
    private final int perLevelIncrement;

    public SpellRequirement(Map<Skill, Integer> baseRequirements, int perLevelIncrement) {
        this.baseRequirements = baseRequirements;
        this.perLevelIncrement = perLevelIncrement;
    }

    public Map<Skill, Integer> getRequirementsForSpellLevel(int spellLevel) {
        Map<Skill, Integer> adjustedRequirements = new HashMap<>();
        for (Map.Entry<Skill, Integer> entry : baseRequirements.entrySet()) {
            int baseValue = entry.getValue();
            int totalRequirement = baseValue + ((spellLevel - 1) * perLevelIncrement);
            adjustedRequirements.put(entry.getKey(), totalRequirement);
        }
        return adjustedRequirements;
    }

    public Map<Skill, Integer> getBaseRequirements() {
        return baseRequirements;
    }


    public int getPerLevelIncrement() {
        return perLevelIncrement;
    }

    @Override
    public String toString() {
        return "SpellRequirement{" +
                "baseRequirements=" + baseRequirements +
                ", perLevelIncrement=" + perLevelIncrement +
                '}';
    }
}
