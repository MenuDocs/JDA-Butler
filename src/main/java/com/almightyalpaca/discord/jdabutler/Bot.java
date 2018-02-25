package com.almightyalpaca.discord.jdabutler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import com.almightyalpaca.discord.jdabutler.commands.Dispatcher;
import com.almightyalpaca.discord.jdabutler.config.Config;
import com.almightyalpaca.discord.jdabutler.config.ConfigFactory;
import com.almightyalpaca.discord.jdabutler.config.exception.KeyNotFoundException;
import com.almightyalpaca.discord.jdabutler.config.exception.WrongTypeException;
import com.almightyalpaca.discord.jdabutler.util.logging.WebhookAppender;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.kantenkugel.discordbot.jdocparser.JDoc;
import com.kantenkugel.discordbot.versioncheck.VersionCheckerRegistry;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import okhttp3.OkHttpClient;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Bot
{

    public static Config config;
    public static Dispatcher dispatcher;
    public static final String INVITE_LINK = "https://discord.gg/0hMr4ce0tIk3pSjp";
    public static JDAImpl jda;

    public static OkHttpClient httpClient;

    public static EventListener listener;

    public static final Logger LOG = (Logger) LoggerFactory.getLogger(Bot.class);

    public static TextChannel getChannelExperimental()
    {
        return Bot.jda.getTextChannelById("289742061220134912");
    }

    public static Guild getGuildJda()
    {
        return Bot.jda.getGuildById("125227483518861312");
    }

    public static Role getRoleBots()
    {
        return Bot.getGuildJda().getRoleById("125616720156033024");
    }

    public static Role getRoleExperimentalUpdates()
    {
        return Bot.getGuildJda().getRoleById("289744006433472513");
    }

    public static Role getRoleHelper()
    {
        return Bot.getGuildJda().getRoleById("183963327033114624");
    }

    public static Role getRoleJdaFanclub()
    {
        return Bot.getGuildJda().getRoleById("169558668126322689");
    }

    public static Role getRoleStaff()
    {
        return Bot.getGuildJda().getRoleById("169481978268090369");
    }

    public static String hastebin(final String text)
    {
        try
        {
            return "https://hastebin.com/" + Unirest.post("https://hastebin.com/documents").header("User-Agent", "Mozilla/5.0 JDA-Butler").header("Content-Type", "text/plain").body(text).asJson().getBody().getObject().getString("key");
        }
        catch (final UnirestException e)
        {
            Bot.LOG.error("Error while creating hastebin link", e);
            return null;
        }
    }

    public static boolean isAdmin(final User user)
    {
        final Member member = Bot.getGuildJda().getMember(user);
        return member != null && member.getRoles().contains(Bot.getRoleStaff());
    }

    public static boolean isHelper(final User sender)
    {
        return Bot.getGuildJda().isMember(sender) && (Bot.isAdmin(sender) || Bot.getGuildJda().getMember(sender).getRoles().contains(Bot.getRoleHelper()));
    }

    public static void main(final String[] args) throws JsonIOException, JsonSyntaxException, WrongTypeException, KeyNotFoundException, IOException, LoginException, IllegalArgumentException, InterruptedException, SecurityException
    {
        Bot.httpClient = new OkHttpClient.Builder().build();

        EventListener.executor.submit(JDoc::init);

        Bot.config = ConfigFactory.getConfig(new File("config.json"));

        final JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setAudioEnabled(false);
        builder.setBulkDeleteSplittingEnabled(false);

        final String token = Bot.config.getString("discord.token", "Your token");
        builder.setToken(token);

        Bot.config.save();
        Bot.listener = new EventListener();
        builder.addEventListener(Bot.listener);
        builder.addEventListener(Bot.dispatcher = new Dispatcher());

        builder.setGame(Game.playing("JDA"));

        Bot.jda = (JDAImpl) builder.buildBlocking();

        if(Bot.config.getBoolean("webhook.enabled", false)) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

            ThresholdFilter filter = new ThresholdFilter();
            filter.setLevel(Bot.config.getString("webhook.level"));
            filter.setContext(lc);
            filter.start();

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setPattern(Bot.config.getString("webhook.pattern"));
            encoder.setContext(lc);
            encoder.start();

            WebhookAppender appender = new WebhookAppender();
            appender.setEncoder(encoder);
            appender.addFilter(filter);
            appender.setWebhookUrl(Bot.config.getString("webhook.webhookurl"));
            appender.setName("ERROR_WH");
            appender.setContext(lc);
            appender.start();

            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.addAppender(appender);
        }

        VersionCheckerRegistry.init();
    }

    public static void shutdown()
    {
        Bot.jda.removeEventListener(Bot.jda.getRegisteredListeners());

        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (final InterruptedException ignored)
        {}

        Bot.jda.shutdown();
    }
}
