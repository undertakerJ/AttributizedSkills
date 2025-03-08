package net.lumi_noble.attributizedskills.client.screen;

import net.lumi_noble.attributizedskills.client.screen.button.StatIconButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
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
            }, supplier -> Component.empty()));
        }
    }
}
