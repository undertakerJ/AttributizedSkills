package net.lumi_noble.attributizedskills.common.compat;

import net.lumi_noble.attributizedskills.common.skill.Skill;

import java.util.Map;

public class ApothRarityRequirement {
    private final Map<Skill, Float> baseRequirements;

    public ApothRarityRequirement(Map<Skill, Float> baseRequirements) {
        this.baseRequirements = baseRequirements;
    }

    public Map<Skill, Float> getBaseRequirements() {
        return baseRequirements;
    }

    @Override
    public String toString() {
        return "ApothRarityRequirement{" +
                "baseRequirements=" + baseRequirements +
                '}';
    }

}
