package net.lumi_noble.attributizedskills.common.commands.common;

import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

public class GetCommand {
	
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("get_skill")
            .then(Commands.argument("player", EntityArgument.player())
            .then(Commands.argument("skill", EnumArgument.enumArgument(Skill.class))
            .executes(GetCommand::execute)));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        Skill skill = context.getArgument("skill", Skill.class);
        int level = SkillModel.get(player).getSkillLevel(skill);
        context.getSource().sendSuccess( Component.literal(player.getName().getString())
                .append(" ")
                .append(Component.translatable("message.command_get"))
                .append(" ")
                .append(Component.translatable(skill.displayName))
                .append(" ")
                .append(Component.literal(String.valueOf(level))), true);

        return level;
    }

}
