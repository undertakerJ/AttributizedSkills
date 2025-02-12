package net.lumi_noble.attributizedskills.common.data;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.loot.AddItemModifier;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.ArrayList;
import java.util.Arrays;

public class AttributizedSkillsDungeonLoot extends GlobalLootModifierProvider {

	public AttributizedSkillsDungeonLoot(DataGenerator gen, String modid) {
		super(gen, modid);
	}

	public static final ArrayList<ResourceLocation> LOOT_TABLES =
			new ArrayList<>(
					Arrays.asList(
							BuiltInLootTables.ANCIENT_CITY,
							BuiltInLootTables.SIMPLE_DUNGEON,
							BuiltInLootTables.BURIED_TREASURE,
							BuiltInLootTables.DESERT_PYRAMID,
							BuiltInLootTables.JUNGLE_TEMPLE,
							BuiltInLootTables.STRONGHOLD_LIBRARY,
							BuiltInLootTables.WOODLAND_MANSION));

	@Override
	protected void start() {
		for (ResourceLocation lootTable : LOOT_TABLES) {
      add(
          "larval_tear_" + lootTable.getPath(),
          new AddItemModifier(
              new LootItemCondition[] {
                LootItemRandomChanceCondition.randomChance(0.4f).build(),
                LootTableIdCondition.builder(lootTable).build()
              },
              new ItemStack(
                  AttributizedSkills.LARVAL_TEAR.get(), 1)));
		}
	}
}
