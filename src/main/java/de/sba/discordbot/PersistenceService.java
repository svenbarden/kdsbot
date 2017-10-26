package de.sba.discordbot;

import de.sba.discordbot.model.AutoTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class PersistenceService {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<AutoTopic> findTopics(int dayOfMonth, int month) {
        Query query = entityManager.createQuery("SELECT t FROM AutoTopic t WHERE t.dayOfMonth = :dayOfMonth AND t.month = :month");
        query.setParameter("dayOfMonth", dayOfMonth).setParameter("month", month);
        return query.getResultList();
    }

    private boolean checkExists(int dayOfMonth, int month, String topic) {
        Query query = entityManager.createQuery("SELECT count(t) FROM AutoTopic t WHERE t.dayOfMonth = :dayOfMonth AND t.month = :month AND topic = :topic");
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
            entityManager.persist(autoTopic);
            check = true;
        }
        return check;
    }

    @Transactional
    public void persist(Object object) {
        entityManager.persist(object);
    }

    @Transactional
    public int executeUpdate(Query query) {
        return query.executeUpdate();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
