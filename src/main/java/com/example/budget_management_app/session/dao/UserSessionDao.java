package com.example.budget_management_app.session.dao;

import com.example.budget_management_app.session.domain.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserSessionDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<UserSession> findByToken(String token) {
        List<UserSession> userSessions = em.createQuery("SELECT r FROM UserSession r WHERE r.refreshToken =  :token", UserSession.class)
                .setParameter("token", token)
                .getResultList();
        return userSessions.stream().findFirst();
    }

    public UserSession save(UserSession session) {
        em.persist(session);
        em.flush();
        return session;
    }
}
