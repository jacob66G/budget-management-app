package com.example.budget_management_app.category.dao;

import com.example.budget_management_app.category.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDao {

    @PersistenceContext
    private EntityManager em;

    public List<Category> findByUser(Long userId, String type) {
        return em.createQuery("SELECT c FROM Category c WHERE c.user.id = :userId AND (:type IS NULL OR LOWER(c.type) = LOWER(cast(:type as text))) ", Category.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }

    public Optional<Category> findByIdAndUser(Long categoryId, Long userId) {
        List<Category> result = em.createQuery("SELECT c FROM Category c WHERE c.id =: categoryId AND c.user.id = :userId", Category.class)
                .setParameter("categoryId", categoryId)
                .setParameter("userId", userId)
                .getResultList();

        return result.stream().findFirst();
    }

    public boolean existsByNameAndUser(String name, Long userId, Long existingCategoryId) {
        String query = "SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId AND LOWER(c.name) = LOWER(:name) AND (:existingCategoryId IS NULL OR c.id != :existingCategoryId)";

        Long count = em.createQuery(query, Long.class)
                .setParameter("userId", userId)
                .setParameter("name", name)
                .setParameter("existingCategoryId", existingCategoryId)
                .getSingleResult();

        return count > 0;
    }

    public List<Long> getUserCategoryIds(Long userId) {

        return em.createQuery("""
                    SELECT c.id FROM Category c
                    WHERE c.user.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Category save(Category category) {
        em.persist(category);
        em.flush();
        return category;
    }

    public Category update(Category category) {
        return em.merge(category);
    }

    public void delete(Category category) {
        em.remove(category);
    }

    public void deleteAllByUser(Long userId) {
        em.createQuery("DELETE FROM Category c WHERE c.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
