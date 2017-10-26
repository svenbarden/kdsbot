package de.sba.discordbot.listener;

import de.sba.discordbot.PersistenceService;
import de.sba.discordbot.model.GameLog;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.List;

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
            Query updateQuery = persistenceService.getEntityManager().createQuery("UPDATE GameLog g SET g.end = :end WHERE g.user = :user AND g.end IS NULL");
            updateQuery.setParameter("user", user).setParameter("end", new Timestamp(System.currentTimeMillis()));
            int updated = persistenceService.executeUpdate(updateQuery);
            LOGGER.debug("updated {} game entries for {}", updated, userName);
        } else {
            String game = member.getGame().getName();
            LOGGER.debug("user {} is playing {}", userName, game);
            Query query = persistenceService.getEntityManager().createQuery("SELECT g FROM GameLog g WHERE g.user = :user AND g.game = :game AND g.start IS NULL");
            query.setParameter("user", user).setParameter("game", game);
            List<GameLog> gameLogs = query.getResultList();
            if (gameLogs.size() == 0) {
                LOGGER.debug("found no game in database for user {}", userName);
                GameLog gameLog = new GameLog();
                gameLog.setStart(new Timestamp(System.currentTimeMillis()));
                gameLog.setGame(game);
                gameLog.setUser(user);
                persistenceService.persist(gameLog);
            }
            Query updateQuery = persistenceService.getEntityManager().createQuery("UPDATE GameLog g SET g.end = :end WHERE g.user = :user AND NOT g.game = :game AND g.end IS NULL");
            updateQuery.setParameter("user", user).setParameter("game", game).setParameter("end", new Timestamp(System.currentTimeMillis()));
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

    @SuppressWarnings("unchecked")
    public GameLog getForUser(String userId) {
        return (GameLog) persistenceService.getEntityManager().createQuery("SELECT g FROM GameLog g WHERE g.end IS NULL AND g.user = :user")
                .setParameter("user", userId).getSingleResult();
    }
}
