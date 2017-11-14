package de.sba.discordbot.listener;

import de.sba.discordbot.PersistenceService;
import de.sba.discordbot.model.GameLog;
import de.sba.discordbot.model.GameLogResult;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

@Service
public class GameLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLogService.class);
    private PersistenceService persistenceService;
    private JDA client;

    @Autowired
    public GameLogService(PersistenceService persistenceService, JDA client) {
        this.persistenceService = persistenceService;
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    private void log(Member member) {
        String user = member.getUser().getId();
        String userName = member.getEffectiveName();
        LOGGER.debug("logging for user {} with id {}", userName, user);
        if(member.getGame() == null) {
            LOGGER.debug("user {} has no game. updating all end dates", userName);
            Query updateQuery = persistenceService.getEntityManager().createQuery("UPDATE GameLog g SET g.end = :endDate WHERE g.user = :user AND g.end IS NULL");
            updateQuery.setParameter("user", user).setParameter("endDate", new Timestamp(System.currentTimeMillis()));
            int updated = persistenceService.executeUpdate(updateQuery);
            LOGGER.debug("updated {} game entries for {}", updated, userName);
        } else {
            String game = member.getGame().getName();
            LOGGER.debug("user {} is playing {}", userName, game);
            Query query = persistenceService.getEntityManager().createQuery("SELECT g FROM GameLog g WHERE g.user = :user AND g.game = :game AND g.end IS NULL");
            query.setParameter("user", user).setParameter("game", game);
            List<GameLog> gameLogs = query.getResultList();
            if (gameLogs.size() == 0) {
                LOGGER.debug("found no game in database for user {}", userName);
                GameLog gameLog = new GameLog();
                gameLog.setStart(new Timestamp(System.currentTimeMillis()));
                gameLog.setGame(game);
                gameLog.setUser(user);
                persistenceService.persist(gameLog);
            } else if(gameLogs.size() > 1) {
                //db fehler, schnell fixen!
                for (int i = 1; i < gameLogs.size(); i++) {
                    gameLogs.get(i).setEnd(new Timestamp(System.currentTimeMillis()));
                    persistenceService.persist(gameLogs.get(i));
                }
            }
            Query updateQuery = persistenceService.getEntityManager().createQuery("UPDATE GameLog g SET g.end = :endDate WHERE g.user = :user AND NOT g.game = :game AND g.end IS NULL");
            updateQuery.setParameter("user", user).setParameter("game", game).setParameter("endDate", new Timestamp(System.currentTimeMillis()));
            int updated = persistenceService.executeUpdate(updateQuery);
            LOGGER.debug("updated {} different game logs for {}", updated, userName);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 1000 * 60 * 2)
    public void logCurrentGames() {
        LOGGER.trace("logging game state");
        client.getGuilds().stream().filter(guild -> guild.getName().equals("Der Kult")).forEach(guild -> {
            guild.getMembers().forEach(this::log);
        });
    }

    @Transactional(readOnly = true)
    public List<GameLog> getTopByGame(String game, int top) {
        if(top <= 0) {
            top = 3;
        }
        Query query = persistenceService.getEntityManager().createQuery("SELECT g.user, sum(case when g.end is null then CURRENT_TIMESTAMP else g.end end - g.start) as duration FROM GameLog g WHERE g.game LIKE :game GROUP BY g.user ORDER BY duration DESC");
        query.setParameter("game", game);
        query.setMaxResults(top);
        List resultList = query.getResultList();

        System.out.println(resultList);

        Query query2 = persistenceService.getEntityManager().createQuery("SELECT g.user, g.start, g.end, EXTRACT(EPOCH FROM g.end) - EXTRACT(EPOCH FROM g.start) as duration FROM GameLog g WHERE g.game LIKE :game ");
        query2.setParameter("game", game);
        List resultList2 = query2.getResultList();
        System.out.println(resultList2);
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    public GameLog getForUser(String userId) {
        return (GameLog) persistenceService.getEntityManager().createQuery("SELECT g FROM GameLog g WHERE g.end IS NULL AND g.user = :user")
                .setParameter("user", userId).getSingleResult();
    }

    @Transactional(readOnly = true)
    public GameLogResult getToday(int diff) {
        LOGGER.debug("get today game log");
        diff = -Math.abs(diff);
        Query query = persistenceService.getEntityManager().createQuery("SELECT g FROM GameLog g WHERE g.start >= :startOfDay AND g.start <= :endOfDay AND (g.end IS NULL OR g.end <= :endOfDay)");
        Date date = DateUtils.setSeconds(DateUtils.setMinutes(DateUtils.setHours(new Date(), 0), 0), 0);
        long endOfDayMillis;
        if(diff < 0) {
            date = DateUtils.addDays(date, diff);
            endOfDayMillis = DateUtils.setHours(DateUtils.setMinutes(DateUtils.setSeconds(date, 59), 59), 23).getTime();
        } else {
            endOfDayMillis = System.currentTimeMillis();
        }
        Timestamp startOfDay = new Timestamp(date.getTime());
        Timestamp endOfDay = new Timestamp(endOfDayMillis);
        query.setParameter("startOfDay", startOfDay).setParameter("endOfDay", endOfDay);

        List<GameLog> gameLogs = query.getResultList();
        Map<String, Map<String, MutableLong>> resultMap = new LinkedHashMap<>();
        long startOfDayMillis = startOfDay.getTime();
        for (GameLog gameLog : gameLogs) {
            LOGGER.trace("check gamelogs for user {} and game {} from {} to {}", gameLog.getUser(), gameLog.getGame(), gameLog.getStart(), gameLog.getEnd());
            Map<String, MutableLong> userMap = resultMap.computeIfAbsent(gameLog.getUser(), k -> new LinkedHashMap<>());
            MutableLong currentDuration = userMap.computeIfAbsent(gameLog.getGame(), k -> new MutableLong(0));
            LOGGER.trace("current duration is {}", currentDuration);
            long start = gameLog.getStart().getTime();
            if(start < startOfDayMillis) {
                LOGGER.trace("set start date to todayStart");
                start = startOfDayMillis;
            }
            long end = ObjectUtils.defaultIfNull(gameLog.getEnd(), endOfDay).getTime();
            long addedDuration = end - start;
            LOGGER.trace("add duration {}", addedDuration);
            currentDuration.add(addedDuration);
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT, Locale.GERMAN);
        GameLogResult result = new GameLogResult()
                .setData(resultMap)
                .setFrom(dateFormat.format(startOfDay))
                .setTo(dateFormat.format(endOfDay));
        LOGGER.debug("return map {}", resultMap);
        return result;
    }
}
