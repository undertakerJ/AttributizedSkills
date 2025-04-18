package net.lumi_noble.attributizedskills.common.compat;

import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ApothRarityRequirement {
    private final Map<Skill, Integer> baseRequirements;

    public ApothRarityRequirement(Map<Skill, Integer> baseRequirements) {
        this.baseRequirements = baseRequirements;
    }

    public Map<Skill, Integer> getBaseRequirements() {
        return baseRequirements;
    }

    @Override
    public String toString() {
        return "ApothRarityRequirement{" +
                "baseRequirements=" + baseRequirements +
                '}';
    }

}
