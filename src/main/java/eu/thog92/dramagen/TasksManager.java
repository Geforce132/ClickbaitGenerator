package eu.thog92.dramagen;

import eu.thog92.dramagen.task.ScheduledTask;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TasksManager
{

    private final Config config;
    private Twitter twitter;
    private Dictionary dictionary;
    private ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(4);

    private HashMap<ScheduledTask, ScheduledFuture<?>> activeTasks = new HashMap<ScheduledTask, ScheduledFuture<?>>();


    public TasksManager(Config config) throws IOException
    {
        this.config = config;
        this.loadConfig();
        this.dictionary = Dictionary.getInstance();
        this.dictionary.setDir(new File("data"));
        this.dictionary.loadCombinaisons();
        this.dictionary.loadBlackList();
    }

    private void loadConfig() throws IOException
    {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(config.debugTwitter)
                .setOAuthConsumerKey(config.consumerKey)
                .setOAuthConsumerSecret(config.consumerSecret)
                .setOAuthAccessToken(config.accessToken)
                .setOAuthAccessTokenSecret(config.accessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        this.twitter = tf.getInstance();
    }

    public void reload() throws IOException
    {
        System.out.println("Reloading Config...");
        this.loadConfig();
        System.out.println("Reloading Dictionary...");
        this.dictionary.reload();
        System.out.println("Config Reloaded");
    }

    public void scheduleTask(ScheduledTask task)
    {
        System.out.println("Scheduling " + task.getName() + "...");
        this.activeTasks.put(task, scheduler.scheduleAtFixedRate(task, 0,
                task.getDelay(), TimeUnit.SECONDS));
    }

    public void resetExecutorService()
    {
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(100);
    }

    public void onFinishTask(ScheduledTask task)
    {
        if (task.isCancelled())
        {
            if (this.activeTasks.get(task) != null)
            {
                this.activeTasks.remove(task).cancel(true);
            }
        }
    }

    public Twitter getTwitter()
    {
        return twitter;
    }

    public Config getConfig()
    {
        return config;
    }
}
