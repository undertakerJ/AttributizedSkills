package net.lumi_noble.attributizedskills.common.compat;


import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

public class CuriosCompat {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChangeCurio(CurioChangeEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
        	Player player = (Player) event.getEntity();
            
            if (!player.isCreative())
            {
                ItemStack item = event.getTo();
                
                if (!SkillModel.get(player).canUseItem(player, item))
                {
                	// curios are always dropped.
                    player.drop(item.copy(), false);
                    item.setCount(0);
                }
            }
        }
    }
}
