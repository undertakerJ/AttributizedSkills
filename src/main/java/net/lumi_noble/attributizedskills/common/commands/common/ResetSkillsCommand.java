package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ResetSkillsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("reset_skills")
                .requires(source -> source.hasPermission(2)) // Требует уровень прав 2 (админ)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            resetSkills(player);
                            context.getSource().sendSuccess(Component.literal("Skills reset for " + player.getName().getString()), true);
                            return 1;
                        }));
    }

    private static void resetSkills(ServerPlayer player) {
        SkillModel skillModel = SkillModel.get(player);
        if (!player.isCreative()) {
            player.giveExperienceLevels(skillModel.getTotalLevel());
        }
        skillModel.resetSkills(player);
        SyncToClientPacket.send(player);
    }
}

