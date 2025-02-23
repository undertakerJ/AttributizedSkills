package net.lumi_noble.attributizedskills.common.item;

import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class TearOfTheGoddessItem extends Item {
    public TearOfTheGoddessItem() {
    super(new Properties().stacksTo(4).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if (!(pPlayer instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemstack);
        }
        serverPlayer.displayClientMessage(Component.translatable("tear_of_the_goddess.add").withStyle(ChatFormatting.AQUA), true);
        serverPlayer.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 1f);
        SkillModel model = SkillModel.get(serverPlayer);
        model.addTearPoints(1);
        itemstack.shrink(1);
        SyncToClientPacket.send(serverPlayer);
        return InteractionResultHolder.consume(itemstack);
    }
}
