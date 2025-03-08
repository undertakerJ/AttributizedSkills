package net.lumi_noble.attributizedskills;

import net.lumi_noble.attributizedskills.client.KeyHandler;
import net.lumi_noble.attributizedskills.client.Tooltip;
import net.lumi_noble.attributizedskills.client.screen.InventoryTabs;
import net.lumi_noble.attributizedskills.common.EventHandler;
import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.commands.ModCommands;
import net.lumi_noble.attributizedskills.common.compat.CuriosCompat;
import net.lumi_noble.attributizedskills.common.compat.IronSpellsCompat;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.effects.AttributizedSkillsEffects;
import net.lumi_noble.attributizedskills.common.item.LarvalTearItem;
import net.lumi_noble.attributizedskills.common.item.TearOfTheGoddessItem;
import net.lumi_noble.attributizedskills.common.loot.ModLootModifiers;
import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(AttributizedSkills.MOD_ID)
public class AttributizedSkills {
  public static final String MOD_ID = "attributizedskills";

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, AttributizedSkills.MOD_ID);
  public static final RegistryObject<Item> LARVAL_TEAR =
      ITEMS.register(
          "larval_tear",
          () ->
              new LarvalTearItem(
                  new Item.Properties()
                      .stacksTo(16)
                      .rarity(Rarity.RARE)));
  public static final RegistryObject<Item> TEAR_OF_THE_GODDESS =
      ITEMS.register("tear_of_the_goddess", TearOfTheGoddessItem::new);

  public AttributizedSkills() {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    AttributizedSkillsEffects.MOB_EFFECTS.register(modEventBus);
    ITEMS.register(modEventBus);
    modEventBus.addListener(this::commonSetup);
    modEventBus.addListener(this::clientSetup);
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ASConfig.SERVER_CONFIG_SPEC);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ASConfig.getClientConfig());

    ModAttributes.register(modEventBus);
    ModLootModifiers.register(modEventBus);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    ModNetworking.register();
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new ModCommands());

    if (ModList.get().isLoaded("curios")) {
      MinecraftForge.EVENT_BUS.register(new CuriosCompat());
    }
    if (ModList.get().isLoaded("irons_spellbooks")) {
      MinecraftForge.EVENT_BUS.register(new IronSpellsCompat());
    }
  }

  private void clientSetup(final FMLClientSetupEvent event) {
    ASConfig.loadClient();
    MinecraftForge.EVENT_BUS.register(new InventoryTabs());
    MinecraftForge.EVENT_BUS.register(new KeyHandler());
    MinecraftForge.EVENT_BUS.register(new Tooltip());
  }

}
