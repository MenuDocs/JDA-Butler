package com.almightyalpaca.discord.jdabutler.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.Arrays;
import java.util.List;

public class ErrFilter extends Filter<ILoggingEvent>
{
    private static final List<Level> allowed = Arrays.asList(Level.WARN, Level.ERROR);
    @Override
    public FilterReply decide(ILoggingEvent event)
    {
        return ErrFilter.allowed.contains(event.getLevel()) ? FilterReply.NEUTRAL : FilterReply.DENY;
    }
}