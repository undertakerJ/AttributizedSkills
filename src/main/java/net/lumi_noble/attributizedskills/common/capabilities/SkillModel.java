package net.lumi_noble.attributizedskills.common.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Requirement;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.lumi_noble.attributizedskills.common.util.CalculateAttributeValue;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import static net.lumi_noble.attributizedskills.common.attributes.ModAttributes.getModifierUUIDForSkill;
import static net.lumi_noble.attributizedskills.common.commands.common.SetCommand.getAttributeForSkill;

@AutoRegisterCapability
public class SkillModel implements INBTSerializable<CompoundTag> {
	
	public static final MutableComponent MAIN_HAND_WARNING = Component.translatable("overlay.main_hand_message");
	public static final MutableComponent OFF_HAND_WARNING = Component.translatable("overlay.off_hand_message");
	public static final MutableComponent GENERIC_WARNING = Component.translatable("overlay.default_message");

	public static final int EFFECT_DURATION = Integer.MAX_VALUE;

	private int[] skillLevels = new int[] { 1, 1, 1, 1, 1, 1 };
	private int totalLevel = 6;

	private int tearPoints = 0;

	public int getSkillLevel(Skill skill) {
		return skillLevels[skill.index];
	}
	
	public int getTotalLevel() {
		return totalLevel;
	}

	public int getTearPoints() {return tearPoints;}

	public boolean underMaxTotal() {
		return totalLevel < ASConfig.getMaxLevelTotal();
	}


	public void setSkillLevel(Skill skill, int level, ServerPlayer player) {
		skillLevels[skill.index] = level;
		totalLevel += level - skillLevels[skill.index];
		updateTotalLevel();
	}

	public void increaseSkillLevel(Skill skill, ServerPlayer player) {
		skillLevels[skill.index]++;
		totalLevel++;
		updateTotalLevel();

	}

	public void updateTotalLevel() {
		totalLevel = 0;
		for (int level : this.skillLevels) {
			totalLevel += level;
		}
	}

	public void resetSkills(ServerPlayer player) {
		this.skillLevels = new int[] { 1, 1, 1, 1, 1, 1 };
		updateTotalLevel();
		ModAttributes.resetPlayerAttributes(player);
	}

	public void setTearPoints(int points) {
		this.tearPoints = Math.max(0, points);
	}
	public void addTearPoints(int tearPoints){
		this.tearPoints += Math.max(0, tearPoints);
	}

	public boolean canUseItem(Player player, ItemStack item) {
		return canUse(player, ForgeRegistries.ITEMS.getKey(item.getItem()));
	}

	public boolean canUseBlock(Player player, Block block) {
		return canUse(player, ForgeRegistries.BLOCKS.getKey(block));
	}

	public boolean canUseEntity(Player player, Entity entity) {
		return canUse(player, ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
	}

	public boolean canUseItemInSlot(Player player, ItemStack itemStack, EquipmentSlot slot) {
		
		ResourceLocation itemLoc = ForgeRegistries.ITEMS.getKey(itemStack.getItem());

		if (isBlacklisted(itemLoc)) {
			return true;
		}

		Requirement[] requirements = ASConfig.getItemRequirements(itemLoc);

		if (requirements != null) {
			for (Requirement requirement : requirements) {

				if (getSkillLevel(requirement.getSkill()) < requirement.getLevel()) {

					if (player instanceof ServerPlayer) {
						displayUnmetRequirementMessage((ServerPlayer) player);
					}

					return false;
				}
			}
		}

		if (itemStack.isEnchanted()) {
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(itemStack);
			Map<Requirement[], Integer> enchantRequirements = new HashMap<>();
			
			if (!enchants.isEmpty()) {
				
				// collect requirements for each enchantment on the item.
				for (Enchantment enchant : enchants.keySet()) {
					
					// only replace requirements if enchant level is higher
					int enchantLevel = enchants.get(enchant);
					Integer oldValue = enchantRequirements.put(ASConfig.getEnchantmentRequirements(ForgeRegistries.ENCHANTMENTS.getKey(enchant)), enchantLevel);
					
					if (oldValue != null && oldValue.intValue() > enchantLevel) {
						enchantRequirements.put(ASConfig.getEnchantmentRequirements(ForgeRegistries.ENCHANTMENTS.getKey(enchant)), oldValue);
					}
				}

				if (!enchantRequirements.isEmpty() && enchantRequirements != null) {
					for (Requirement[] requirementsPerEnchant : enchantRequirements.keySet()) {
						if (requirementsPerEnchant != null) {
							for (Requirement enchantRequirement : requirementsPerEnchant) {
								
								// check if player can use enchanted item
								double levelRequirement = enchantRequirement.getLevel();
								int enchantLevel = enchantRequirements.get(requirementsPerEnchant);
								
								double finalValue = enchantLevel == 1 ? levelRequirement : levelRequirement + (enchantLevel * ASConfig.getEnchantmentRequirementIncrease());
								
								if (!(SkillModel.get(player).getSkillLevel(enchantRequirement.getSkill()) >= finalValue)) {
									
									if (player instanceof ServerPlayer) {
										displayUnmetRequirementMessage(slot, (ServerPlayer) player);
									}
									
									return false;
								}
							}
						}
					}
				}
			}
		}
		
		return true;
	}

	private boolean canUse(Player player, ResourceLocation resource) {
		
		// check if blacklisted
		if (isBlacklisted(resource))  {
			return true;
		}

		Requirement[] requirements = ASConfig.getItemRequirements(resource);

		if (requirements != null) {
			for (Requirement requirement : requirements) {

				if (getSkillLevel(requirement.getSkill()) < requirement.getLevel()) {

					if (player instanceof ServerPlayer) {
						displayUnmetRequirementMessage((ServerPlayer) player);
					}

					return false;
				}
			}
		}

		return true;
	}
	
	private void displayUnmetRequirementMessage(EquipmentSlot slot, ServerPlayer player) {
		switch (slot) {
		case MAINHAND: {
			player.displayClientMessage(
					MAIN_HAND_WARNING.withStyle(ChatFormatting.RED), true);
			break;
		}
		case OFFHAND: {
			player.displayClientMessage(
					OFF_HAND_WARNING.withStyle(ChatFormatting.RED), true);
			break;
		}
		default: {
			player.displayClientMessage(
					GENERIC_WARNING.withStyle(ChatFormatting.RED), true);
		}
		}
	}
	
	private void displayUnmetRequirementMessage(ServerPlayer player) {
		player.displayClientMessage(GENERIC_WARNING.withStyle(ChatFormatting.RED), true);
	}

	public static SkillModel get(Player player) {
		return player.getCapability(SkillCapability.SKILL_MODEL)
				.orElseThrow(() -> new IllegalArgumentException(
				"Player " + player.getName().getString() + " does not have a Skill Model"));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static SkillModel get() {
		return Minecraft.getInstance().player.getCapability(SkillCapability.SKILL_MODEL)
				.orElseThrow(() -> new IllegalArgumentException("Player does not have a Skill Model"));
	}

	public void copyForRespawns(SkillModel oldSkill, ServerPlayer oldPlayer) {
		this.deserializeNBT(oldSkill.serializeNBT()); // Копируем все данные

		for (Skill skill : Skill.values()) {
			UUID modifierUUID = getModifierUUIDForSkill(skill);
			Attribute attribute = getAttributeForSkill(skill);

			if (attribute != null) {
				AttributeInstance instance = oldPlayer.getAttribute(attribute);

				if (instance == null || instance.getModifier(modifierUUID) == null) {
					this.setSkillLevel(skill, 1, null);

				}
			}
		}

		this.updateTotalLevel(); // Пересчитываем общий уровень
	}


	public void copyForRespawn(SkillModel oldStore) {
		this.deserializeNBT(oldStore.serializeNBT());
	}

	public static boolean isBlacklisted(ResourceLocation loc) {
		return ASConfig.getBlacklist().contains(loc.toString());
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("vigor", skillLevels[0]);
		tag.putInt("strength", skillLevels[1]);
		tag.putInt("mind", skillLevels[2]);
		tag.putInt("dexterity", skillLevels[3]);
		tag.putInt("endurance", skillLevels[4]);
		tag.putInt("intelligence", skillLevels[5]);
		tag.putInt("totalLevel", totalLevel);
		tag.putInt("tearPoints", tearPoints);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		skillLevels[0] = nbt.getInt("vigor");
		skillLevels[1] = nbt.getInt("strength");
		skillLevels[2] = nbt.getInt("mind");
		skillLevels[3] = nbt.getInt("dexterity");
		skillLevels[4] = nbt.getInt("endurance");
		skillLevels[5] = nbt.getInt("intelligence");
		totalLevel = nbt.getInt("totalLevel");
		tearPoints = nbt.getInt("tearPoints");
	}

}
