package net.lumi_noble.attributizedskills.common.commands.common;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.network.packets.SyncToClientPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetTearPointsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("set_tear_points")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("points", IntegerArgumentType.integer(1))
                        .executes(SetTearPointsCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int points = IntegerArgumentType.getInteger(context, "points");

        SkillModel model = SkillModel.get(player);
        model.setTearPoints(points);
        SyncToClientPacket.send(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.set_tear_point.success", points, player.getName().getString()), true);
        return 1;
    }
}
