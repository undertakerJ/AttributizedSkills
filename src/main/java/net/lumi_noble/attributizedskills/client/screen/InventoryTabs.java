package net.lumi_noble.attributizedskills.client.screen;

import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.client.screen.button.StatIconButton;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.Config;

import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class InventoryTabs {

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init event)
    {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen || screen instanceof CuriosScreen) {

            event.addListener(new StatIconButton(0, 0, 16, 16, Component.literal(""), button -> {
                Minecraft.getInstance().setScreen(new SkillScreenV2());
            }));
        }
    }
}
