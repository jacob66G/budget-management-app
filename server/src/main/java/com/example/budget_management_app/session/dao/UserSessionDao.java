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

    public Optional<UserSession> findById(Long id) {
        return Optional.ofNullable(em.find(UserSession.class, id));
    }

    public List<UserSession> findAllByUserId(Long userId) {
        return em.createQuery("SELECT s FROM UserSession s WHERE s.user.id = :userId", UserSession.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public UserSession save(UserSession session) {
        em.persist(session);
        em.flush();
        return session;
    }

    public void deleteAllByUserId(Long userId) {
        em.createQuery("DELETE FROM UserSession s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
