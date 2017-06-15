package com.almightyalpaca.discord.jdabutler.log;

import com.almightyalpaca.discord.jdabutler.Bot;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class LogList implements SimpleLog.LogListener
{

    private static final String LOGFORMAT = "[%time%] [%level%] [%name%]: %text%";
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void onError(final SimpleLog log, final Throwable t)
    {
        log.log(SimpleLog.Level.FATAL, ExceptionUtils.getStackTrace(t));
    }

    @Override
    public void onLog(final SimpleLog log, final SimpleLog.Level level, final Object message)
    {
        Logger logger = LoggerFactory.getLogger(log.name);
        String msg = Objects.toString(message);
        switch (level)
        {
            case TRACE:
                logger.trace(msg);
                break;
            case DEBUG:
                logger.debug(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case WARNING:
                logger.warn(msg);
                break;
            case FATAL:
                logger.error(msg);
                break;
        }
        if(Bot.jda == null)
            return;
        try
        {
            if (level.getPriority() >= SimpleLog.Level.INFO.getPriority())
            {
                String format = "`" + LogList.LOGFORMAT.replace("%time%", LogList.DATEFORMAT.format(new Date())).replace("%level%", level.getTag()).replace("%name%", log.name).replace("%text%", String.valueOf(message)) + "`";
                if (format.length() >= 2000)
                    format = format.substring(0, 1999);
                final TextChannel channel = Bot.getChannelLogs();
                if (channel != null)
                    for (final Message m : new MessageBuilder().append(format).buildAll(MessageBuilder.SplitPolicy.NEWLINE, MessageBuilder.SplitPolicy.SPACE, MessageBuilder.SplitPolicy.ANYWHERE))
                        channel.sendMessage(m).queue();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
