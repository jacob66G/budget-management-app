package com.example.budget_management_app.session.dao;

import com.example.budget_management_app.session.domain.UserSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserSessionDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<UserSession> findById(Long id) {
        return Optional.ofNullable(em.find(UserSession.class, id));
    }

    public UserSession save(UserSession session) {
        em.persist(session);
        em.flush();
        return session;
    }
}
