package com.example.budget_management_app.user.dao;

import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public boolean userExists(Long id) {
        Long count = em.createQuery("""
                SELECT COUNT(u) FROM User u
                WHERE u.id = :id
                """, Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count != null && count == 1;
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = em.createQuery("SELECT u FROM User u WHERE LOWER(u.email) =  LOWER(:email)", User.class)
                .setParameter("email", email)
                .getResultList();
        return users.stream().findFirst();
    }

    public User save(User user) {
        em.persist(user);
        em.flush();
        return user;
    }

    public User update(User user) {
        return em.merge(user);
    }

    public void delete(User user) {
        em.remove(user);
    }

    public List<User> findUsersForDeletion(UserStatus status, Instant cutoffDate) {
        return em.createQuery("SELECT u FROM User u WHERE u.status = :status AND u.requestCloseAt < :cutoff", User.class)
                .setParameter("status", status)
                .setParameter("cutoff", cutoffDate)
                .getResultList();
    }
}
