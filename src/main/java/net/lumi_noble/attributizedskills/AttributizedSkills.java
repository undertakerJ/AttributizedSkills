package net.lumi_noble.attributizedskills;

import net.lumi_noble.attributizedskills.client.KeyHandler;
import net.lumi_noble.attributizedskills.client.Tooltip;
import net.lumi_noble.attributizedskills.client.screen.InventoryTabs;
import net.lumi_noble.attributizedskills.common.CuriosCompat;
import net.lumi_noble.attributizedskills.common.EventHandler;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.item.LarvalTearItem;
import net.lumi_noble.attributizedskills.common.attributes.ModAttributes;
import net.lumi_noble.attributizedskills.common.commands.Commands;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.lumi_noble.attributizedskills.common.loot.ModLootModifiers;
import net.lumi_noble.attributizedskills.common.effects.AttributizedSkillsEffects;
import net.lumi_noble.attributizedskills.common.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
import top.theillusivec4.curios.Curios;

@Mod(AttributizedSkills.MOD_ID)
public class AttributizedSkills
{
    public static final String MOD_ID = "attributizedskills";
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AttributizedSkills.MOD_ID);
    public static final RegistryObject<Item> LARVAL_TEAR = ITEMS.register("larval_tear", () -> new LarvalTearItem(
            new Item.Properties().stacksTo(16)
                    .rarity(Rarity.RARE).tab(CreativeModeTab.TAB_MISC)));
    
    public AttributizedSkills()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AttributizedSkillsEffects.MOB_EFFECTS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.getConfig());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.getClientConfig());

        ModAttributes.register(modEventBus);
        ModLootModifiers.register(modEventBus);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {	
    	Config.load();
        ModNetworking.register();
	    MinecraftForge.EVENT_BUS.register(new EventHandler());
    	MinecraftForge.EVENT_BUS.register(new Commands());

    	if (ModList.get().isLoaded(Curios.MODID)) {
    		MinecraftForge.EVENT_BUS.register(new CuriosCompat());
    	}
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
    	Config.loadClient();
    	MinecraftForge.EVENT_BUS.register(new InventoryTabs());
    	MinecraftForge.EVENT_BUS.register(new KeyHandler());
    	MinecraftForge.EVENT_BUS.register(new Tooltip());
    }
}

