package de.sba.discordbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sba.discordbot.PersistenceService;
import de.sba.discordbot.model.AutoTopic;
import de.sba.discordbot.model.TopicImport;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TopicService implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private TypeReference<List<TopicImport>> TOPIC_IMPORT_TYPE = new TypeReference<List<TopicImport>>() {};
    private static final Map<String, Integer> MONTH_MAP = new HashMap<>();

    private ApplicationContext applicationContext;
    @Autowired
    private PersistenceService persistenceService;
    @Autowired
    private JDA client;
    @Autowired
    private Configuration configuration;

    static {
        MONTH_MAP.put("Januar", 1);
        MONTH_MAP.put("Februar", 2);
        MONTH_MAP.put("März", 3);
        MONTH_MAP.put("April", 4);
        MONTH_MAP.put("Mai", 5);
        MONTH_MAP.put("Juni", 6);
        MONTH_MAP.put("Juli", 7);
        MONTH_MAP.put("August", 8);
        MONTH_MAP.put("September", 9);
        MONTH_MAP.put("Oktober", 10);
        MONTH_MAP.put("November", 11);
        MONTH_MAP.put("Dezember", 12);
    }

    private void importTopics(Resource resource) {
        try {
            List<TopicImport> topics = MAPPER.readValue(resource.getInputStream(), TOPIC_IMPORT_TYPE);
            topics.forEach(this::importTopic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importTopic(TopicImport topicImport) {
        register(Integer.valueOf(topicImport.getDayOfMonth()), getMonth(topicImport.getMonth()), topicImport.getTopic());
    }

    private int getMonth(String month) {
        return MONTH_MAP.get(month);
    }

    @Transactional
    public void reload() {
        try {
            Resource[] resources = applicationContext.getResources("classpath:/META-INF/autotopic/*.json");
            for (Resource resource : resources) {
                importTopics(resource);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<AutoTopic> findTopics(int dayOfMonth, int month) {
        Query query = persistenceService.getEntityManager().createQuery("SELECT t FROM AutoTopic t WHERE t.dayOfMonth = :dayOfMonth AND t.month = :month");
        query.setParameter("dayOfMonth", dayOfMonth).setParameter("month", month);
        return query.getResultList();
    }

    private boolean checkExists(int dayOfMonth, int month, String topic) {
        Query query = persistenceService.getEntityManager().createQuery("SELECT count(t) FROM AutoTopic t WHERE t.dayOfMonth = :dayOfMonth AND t.month = :month AND topic = :topic");
        query.setParameter("dayOfMonth", dayOfMonth).setParameter("month", month).setParameter("topic", topic);
        return ((Long) query.getSingleResult()) > 0;
    }

    @Transactional
    public boolean register(int dayOfMonth, int month, String topic) {
        boolean check = false;
        if(!checkExists(dayOfMonth, month, topic)) {
            AutoTopic autoTopic = new AutoTopic();
            autoTopic.setDayOfMonth(dayOfMonth);
            autoTopic.setMonth(month);
            autoTopic.setTopic(topic);
            persistenceService.persist(autoTopic);
            check = true;
        }
        return check;
    }

    @Transactional(readOnly = true)
    public String getAutoTopic() {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH) + 1;
        List<AutoTopic> topics = findTopics(dayOfMonth, month);
        return topics.stream().map(AutoTopic::getTopic).collect(Collectors.joining(" | "));
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional(readOnly = true)
    public void executeAutoTopic() {
        LOGGER.info("setting auto topics");
        String topic = getAutoTopic();
        if(topic == null || topic.length() == 0) {
            topic = "Happy Sad Tag mit ohne Topic";
        } else {
            topic = "Happy " + topic;
        }
        String finalTopic = topic;
        configuration.getTopic().getAutoTopicTargets().forEach((serverName, channelNames) -> {
            List<Guild> guilds = client.getGuildsByName(serverName, false);
            guilds.forEach(guild -> setAutoTopic(guild, channelNames, finalTopic));
        });
    }

    private void setAutoTopic(Guild guild, List<String> channelNames, String topic) {
        LOGGER.info("set auto topic for guild {} and channels {}", guild.getName(), channelNames);
        channelNames.forEach(channelName -> {
            List<TextChannel> channels = guild.getTextChannelsByName(channelName, false);
            if(channels == null || channels.isEmpty()) {
                LOGGER.warn("channel {} not found for guild {}", channelName, guild.getName());
            } else {
                channels.forEach(textChannel -> {
                    textChannel.getManager().setTopic(topic).complete();
                    textChannel.sendMessageFormat("```\nDaily topic gesetzt: %s\n```", topic).complete();
                });
            }
        });
    }
}
