package net.lumi_noble.attributizedskills.common.attributes;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AttributizedSkills.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AttributizedSkills.MOD_ID);

    public static final RegistryObject<Attribute> VITALITY = create("vitality");
    public static final RegistryObject<Attribute> STRENGTH = create("strength");
    public static final RegistryObject<Attribute> MIND = create("mind");
    public static final RegistryObject<Attribute> DEXTERITY = create("dexterity");
    public static final RegistryObject<Attribute> ENDURANCE = create("endurance");
    public static final RegistryObject<Attribute> INTELLIGENCE = create("intelligence");


    private static RegistryObject<Attribute> create(String name) {
        String descriptionId = "attribute.%s.%s".formatted(AttributizedSkills.MOD_ID, name);
        return ATTRIBUTES.register(
                name, () -> new RangedAttribute(descriptionId, 1d, 1d, 1000d).setSyncable(true));
    }

    public static void register(IEventBus eventBus){
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void attachAttributes(EntityAttributeModificationEvent event) {
        ATTRIBUTES.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(attribute -> {
                    event.getTypes()
                            .forEach(type -> event.add(type, attribute));
                });
    }


    public static final Map<RegistryObject<Attribute>, Skill> ATTRIBUTE_BY_SKILL = Map.of(
            ModAttributes.VITALITY, Skill.VITALITY,
            ModAttributes.STRENGTH,Skill.STRENGTH,
            ModAttributes.MIND,Skill.MIND,
            ModAttributes.DEXTERITY,Skill.DEXTERITY,
            ModAttributes.ENDURANCE,Skill.ENDURANCE,
            ModAttributes.INTELLIGENCE, Skill.INTELLIGENCE
    );

    public static UUID getModifierUUIDForSkill(Skill skill) {
        return switch (skill) {
            case VITALITY -> UUID.fromString("e9810faf-3386-482a-af96-0f8b8b28de04");
            case STRENGTH -> UUID.fromString("b2deb2fe-d35f-42e9-b5fe-c0509ddbe7f9");
            case MIND -> UUID.fromString("f0af87e8-f034-4826-8557-4225f4ef3885");
            case DEXTERITY -> UUID.fromString("77c42b06-b4d3-4e2b-8bf2-e32d8efeee6e");
            case ENDURANCE -> UUID.fromString("ec8e0daf-0229-49bd-ab8d-3c0f578c3e50");
            case INTELLIGENCE -> UUID.fromString("5c783fc7-2705-450d-b1c1-2e49c8527871");
            default -> UUID.randomUUID();
        };
    }

    public static void resetPlayerAttributes(ServerPlayer player) {
        for (Map.Entry<RegistryObject<Attribute>, Skill> entry : ModAttributes.ATTRIBUTE_BY_SKILL.entrySet()) {
            Attribute attribute = entry.getKey().get();
            Skill skill = entry.getValue();
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;

            UUID targetUUID = ModAttributes.getModifierUUIDForSkill(skill);

            for (AttributeModifier mod : new ArrayList<>(instance.getModifiers())) {
                if (mod.getId().equals(targetUUID)) {
                    instance.removeModifier(mod.getId());
                    break;
                }
            }
        }
    }

}
