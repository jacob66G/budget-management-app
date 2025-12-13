package com.example.budget_management_app.notification.dao;

import com.example.budget_management_app.notification.domain.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<Notification> findById(Long id) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.id = :id", Notification.class)
                .setParameter("id", id)
                .getResultList().stream().findFirst();
    }

    public List<Notification> findByUser(Long userId, boolean isRead) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead =: isRead ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("userId", userId)
                .setParameter("isRead", isRead)
                .getResultList();
    }

    public Notification save(Notification notification) {
        em.persist(notification);
        em.flush();
        return notification;
    }

    public Notification update(Notification notification) {
        return em.merge(notification);
    }

    public void markAllAsReadForUser(Long userId) {
        em.createQuery("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public long countUnreadForUser(Long userId) {
        return em.createQuery(
                        "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false",
                        Long.class
                )
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
