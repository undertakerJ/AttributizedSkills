package net.lumi_noble.attributizedskills.common.attributes.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeBonus {
    public final double multiplier;
    public final AttributeModifier.Operation operation;

    public AttributeBonus(double multiplier, AttributeModifier.Operation operation) {
        this.multiplier = multiplier;
        this.operation = operation;
    }
}
