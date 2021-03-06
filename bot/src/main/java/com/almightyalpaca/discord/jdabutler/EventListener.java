package com.almightyalpaca.discord.jdabutler;

import com.almightyalpaca.discord.jdabutler.eval.Engine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

public class EventListener extends ListenerAdapter
{

    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event)
    {
        final Guild guild = event.getGuild();
        if (guild.getId().equals("125227483518861312"))
        {
            final Member member = event.getMember();
            final User user = member.getUser();
            if(user.isBot())
            {
                final AuditableRestAction<Void> action = guild.addRoleToMember(member, Bot.getRoleBots()).reason("Auto Role");
                final String message = String.format("Added %#s (%d) to %s", user, user.getIdLong(), Bot.getRoleBots().getName());
                action.queue(
                        v -> Bot.LOG.info(message),
                        ex -> Bot.LOG.error("Could not add User {} to role {}", user.getName(), Bot.getRoleBots().getName(), ex)
                );
            }
        }
    }

    @Override
    public void onShutdown(final ShutdownEvent event)
    {
        Engine.shutdown();
    }
}
