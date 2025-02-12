package net.lumi_noble.attributizedskills.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.Config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LarvalTearItem extends Item {

	public LarvalTearItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.fail(itemstack);
		}

		TearAction action = Config.getTearAction();
		switch (action) {
			case RESET: {
				player.displayClientMessage(Component.translatable("larvar_tear.reset_successful").withStyle(ChatFormatting.GREEN), true);
				player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 1f);
				SkillModel model = SkillModel.get(serverPlayer);
				model.resetSkills(serverPlayer);
				itemstack.shrink(1);
				return InteractionResultHolder.consume(itemstack);
			}
			case LOOT: {
				if (!level.isClientSide()) {
					ItemStack xpBottles = new ItemStack(Items.EXPERIENCE_BOTTLE, 12);
					if (!player.addItem(xpBottles)) {
						player.drop(xpBottles, false);
					}
				}
				itemstack.shrink(1);
				return InteractionResultHolder.consume(itemstack);
			}
			case OFF: {
				return InteractionResultHolder.fail(itemstack);
			}
			default:
				return InteractionResultHolder.pass(itemstack);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		TearAction action = Config.getTearAction();
		switch (action) {
			case RESET:
				tooltip.add(Component.translatable("attributizedskills.tooltip.larval_tear_reset").withStyle(ChatFormatting.GRAY));
				break;
			case LOOT:
				tooltip.add(Component.translatable("attributizedskills.tooltip.larval_tear_loot").withStyle(ChatFormatting.GRAY));
				break;
			case OFF:
				tooltip.add(Component.translatable("attributizedskills.tooltip.larval_tear_off").withStyle(ChatFormatting.RED));
				break;
		}
	}
}
