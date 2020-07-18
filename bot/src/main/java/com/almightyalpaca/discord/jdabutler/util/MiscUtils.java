package com.almightyalpaca.discord.jdabutler.util;

import com.almightyalpaca.discord.jdabutler.Bot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.menudocs.paste.PasteClient;
import org.menudocs.paste.PasteClientBuilder;
import org.menudocs.paste.PasteHost;
import org.slf4j.Logger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MiscUtils
{
    private static final PasteClient pasteClient = new PasteClientBuilder()
            .setPasteHost(PasteHost.MENUDOCS)
            .setDefaultExpiry("10m")
            .setUserAgent("Mozilla/5.0 JDA-Butler")
            .build();

    public static ThreadFactory newThreadFactory(String threadName)
    {
        return newThreadFactory(threadName, Bot.LOG);
    }

    public static ThreadFactory newThreadFactory(String threadName, boolean isDaemon)
    {
        return newThreadFactory(threadName, Bot.LOG, isDaemon);
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger)
    {
        return newThreadFactory(threadName, logger, true);
    }

    public static ThreadFactory newThreadFactory(String threadName, Logger logger, boolean isdaemon)
    {
        return (r) ->
        {
            Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            t.setUncaughtExceptionHandler((final Thread thread, final Throwable throwable) ->
                    logger.error("There was a uncaught exception in the {} threadpool", thread.getName(), throwable));
            return t;
        };
    }

    public static String hastebin(final String text)
    {
        final String pasteId = pasteClient.createPaste("xml", text).execute();

        return "https://paste.menudocs.org/paste/" + pasteId;
    }

    public static void announce(TextChannel channel, Role role, Message message, boolean slowmode)
    {
        if (slowmode)
        {
            channel.getManager().setSlowmode(30).queue(v -> channel.getManager().setSlowmode(0).queueAfter(2, TimeUnit.MINUTES));
        }

        role.getManager().setMentionable(true).queue(v ->
                channel.sendMessage(message).queue(v2 ->
                        role.getManager().setMentionable(false).queue()
                )
        );
    }
}
