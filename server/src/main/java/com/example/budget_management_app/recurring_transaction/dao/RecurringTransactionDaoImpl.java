package com.example.budget_management_app.recurring_transaction.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RecurringTransactionDaoImpl implements RecurringTransactionDao{

    @PersistenceContext
    private EntityManager em;
    /**
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<Tuple> getSummaryTuplesByUserId(long userId, int page, int limit) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<RecurringTransaction> root = cq.from(RecurringTransaction.class);
        Join<RecurringTransaction, Account> account = root.join("account", JoinType.INNER);
        Join<RecurringTransaction, Category> category = root.join("category", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);

        cq.multiselect(
                root.get("id").alias("recId"),
                root.get("title").alias("title"),
                root.get("amount").alias("amount"),
                root.get("type").alias("type"),
                root.get("isActive").alias("isActive"),
                root.get("description").alias("desc"),
                root.get("recurringInterval").alias("recInterval"),
                root.get("recurringValue").alias("recValue"),
                account.get("id").alias("accountId"),
                account.get("name").alias("accountName"),
                account.get("currency").alias("currency"),
                category.get("id").alias("categoryId"),
                category.get("name").alias("categoryName"),
                category.get("iconPath").alias("iconPath")
        );

        Predicate p = cb.equal(user.get("id"), userId);
        cq.where(p);
        cq.orderBy(cb.desc(root.get("createdAt")));

        int offest = (page - 1) * limit;

        return em.createQuery(cq)
                .setFirstResult(offest)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public Long getSummaryTuplesCountByUserId(long userId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RecurringTransaction> root = countQuery.from(RecurringTransaction.class);
        Join<RecurringTransaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);

        Predicate p = cb.equal(user.get("id"), userId);

        countQuery.select(cb.count(root)).where(p);

        return em.createQuery(countQuery)
                .getSingleResult();
    }

    /**
     * @param id
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<RecurringTransaction> findByIdAndUserId(long id, long userId) {

        List<RecurringTransaction> result = em.createQuery("""
                SELECT r FROM RecurringTransaction r
                JOIN r.account a
                WHERE r.id = :id AND a.user.id = :userId
                """, RecurringTransaction.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.stream().findFirst();
    }

    /**
     * @param recurringTransaction
     * @return
     */
    @Transactional
    @Override
    public RecurringTransaction create(RecurringTransaction recurringTransaction) {
        em.persist(recurringTransaction);
        em.flush();
        return recurringTransaction;
    }

    /**
     * @param recurringTransaction
     */
    @Transactional
    @Override
    public void delete(RecurringTransaction recurringTransaction) {
        em.remove(recurringTransaction);
    }
}
