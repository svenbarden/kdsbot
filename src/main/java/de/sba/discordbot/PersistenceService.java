package de.sba.discordbot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Service
public class PersistenceService {
    @PersistenceContext
    private EntityManager entityManager;

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
