package net.lumi_noble.attributizedskills.common.capabilities;

import java.util.*;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.compat.ApothRarityRequirement;
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
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.fml.ModList;
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

		Map<Skill, Double> totalRequirements = new HashMap<>();

		Requirement[] baseReqs = ASConfig.getItemRequirements(itemLoc);
		if (baseReqs != null) {
			for (Requirement req : baseReqs) {
				totalRequirements.merge(req.getSkill(), req.getLevel(), Double::sum);
			}
		}

		if (ModList.get().isLoaded(Apotheosis.MODID) && player.level() instanceof ServerLevel) {
			try {
				DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(itemStack);
				if (rarityHolder != null) {
					ResourceLocation rarityId = rarityHolder.getId();
					ApothRarityRequirement apothReq = ASConfig.APOTH_RARITY_REQUIREMENTS_MAP.get(rarityId);
					if (apothReq != null) {
						for (Map.Entry<Skill, Integer> entry : apothReq.getBaseRequirements().entrySet()) {
							totalRequirements.merge(entry.getKey(), (double) entry.getValue(), Double::sum);
						}
					}
				}
			} catch (Exception ignored) {}
		}


		if (itemStack.isEnchanted()) {
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(itemStack);

			for (Map.Entry<Enchantment, Integer> enchantEntry : enchants.entrySet()) {
				Enchantment enchant = enchantEntry.getKey();
				int enchantLevel = enchantEntry.getValue();

				Requirement[] enchantReqs = ASConfig.getEnchantmentRequirements(ForgeRegistries.ENCHANTMENTS.getKey(enchant));
				if (enchantReqs != null) {
					for (Requirement req : enchantReqs) {
						double baseLevel = req.getLevel();
						double finalLevel = (enchantLevel == 1)
								? baseLevel
								: baseLevel + (enchantLevel * ASConfig.getEnchantmentRequirementIncrease());

						totalRequirements.merge(req.getSkill(), finalLevel, Double::sum);
					}
				}
			}
		}

		for (Map.Entry<Skill, Double> entry : totalRequirements.entrySet()) {
			Skill skill = entry.getKey();
			double required = entry.getValue();
			int actual = getSkillLevel(skill);

			if (actual < required) {
				if (player instanceof ServerPlayer serverPlayer) {
					displayUnmetRequirementMessage(slot, serverPlayer);
				}
				return false;
			}
		}

		return true;
	}

	private boolean canUse(Player player, ResourceLocation resource) {
		if (isBlacklisted(resource)) {
			return true;
		}

		Map<Skill, Integer> totalRequirements = new HashMap<>();

		Requirement[] baseReqs = ASConfig.getItemRequirements(resource);
		if (baseReqs != null) {
			for (Requirement req : baseReqs) {
				totalRequirements.merge(req.getSkill(), (int) req.getLevel(), Integer::sum);
			}
		}


		if (ModList.get().isLoaded(Apotheosis.MODID) && player.level() instanceof ServerLevel) {
			ItemStack heldItem = player.getMainHandItem();
			if (!heldItem.isEmpty()) {
				try {
					DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(heldItem);
					if (rarity != null) {
						ResourceLocation rarityId = rarity.getId();
						ApothRarityRequirement apothReq = ASConfig.APOTH_RARITY_REQUIREMENTS_MAP.get(rarityId);
						if (apothReq != null) {
							for (Map.Entry<Skill, Integer> entry : apothReq.getBaseRequirements().entrySet()) {
								totalRequirements.merge(entry.getKey(), entry.getValue(), Integer::sum);
							}
						}
					}
				} catch (Exception ignored) {}
			}
		}

		for (Map.Entry<Skill, Integer> entry : totalRequirements.entrySet()) {
			Skill skill = entry.getKey();
			int requiredLevel = entry.getValue();
			if (getSkillLevel(skill) < requiredLevel) {
				if (player instanceof ServerPlayer serverPlayer) {
					displayUnmetRequirementMessage(serverPlayer);
				}
				return false;
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
