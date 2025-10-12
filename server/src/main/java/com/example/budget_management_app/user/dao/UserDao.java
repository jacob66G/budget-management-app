package com.example.budget_management_app.user.dao;

import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = em.createQuery("SELECT u FROM User u WHERE LOWER(u.email) =  LOWER(:email)", User.class)
                .setParameter("email",email)
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
}
