package com.example.budget_management_app.recurring_transaction.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.domain.UpcomingTransactionsTimeRange;
import com.example.budget_management_app.recurring_transaction.dto.UpcomingTransactionFilterParams;
import com.example.budget_management_app.transaction_common.dto.PaginationParams;
import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RecurringTransactionDaoImpl implements RecurringTransactionDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * @param userId
     * @return
     */
    @Override
    public List<Tuple> getSummaryTuplesByUserId(PaginationParams paginationParams, Long userId) {

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
                root.get("nextOccurrence").alias("nextOccurrence"),
                root.get("recurringInterval").alias("recInterval"),
                root.get("recurringValue").alias("recValue"),
                account.get("id").alias("accountId"),
                account.get("name").alias("accountName"),
                account.get("currency").alias("currency"),
                category.get("id").alias("categoryId"),
                category.get("name").alias("categoryName"),
                category.get("iconKey").alias("iconKey")
        );

        Predicate p = cb.equal(user.get("id"), userId);
        cq.where(p);
        cq.orderBy(cb.desc(root.get("createdAt")));

        int limit = paginationParams.getLimit();
        int offest = (paginationParams.getPage() - 1) * limit;

        return em.createQuery(cq)
                .setFirstResult(offest)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public Long getSummaryTuplesCountByUserId(Long userId) {

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
    @Override
    public Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId) {

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
    @Override
    public RecurringTransaction save(RecurringTransaction recurringTransaction) {
        em.persist(recurringTransaction);
        em.flush();
        return recurringTransaction;
    }

    /**
     * @param recurringTransaction
     */
    @Override
    public void delete(RecurringTransaction recurringTransaction) {
        em.remove(recurringTransaction);
    }

    /**
     * @return
     */
    @Override
    public List<RecurringTransaction> searchForRecurringTransactionsToCreate() {

        return em.createQuery("""
                        SELECT r FROM RecurringTransaction r
                        WHERE r.isActive = TRUE AND r.nextOccurrence = :today
                        """, RecurringTransaction.class)
                .setParameter("today", LocalDate.now())
                .getResultList();
    }

    /**
     * @return List of Tuples for upcoming transactions
     */
    @Override
    public List<Tuple> getUpcomingTransactionsTuples(PaginationParams paginationParams,
                                                     UpcomingTransactionFilterParams filterParams,
                                                     Long userId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<RecurringTransaction> root = cq.from(RecurringTransaction.class);
        Join<RecurringTransaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);
        Join<RecurringTransaction, Category> category = root.join("category", JoinType.INNER);

        cq.multiselect(
                root.get("id").alias("recurringTemplateId"),
                root.get("amount").alias("amount"),
                root.get("title").alias("title"),
                root.get("type").alias("type"),
                root.get("nextOccurrence").alias("nextOccurrence"),
                account.get("id").alias("accountId"),
                account.get("name").alias("accountName"),
                account.get("currency").alias("currency"),
                category.get("id").alias("categoryId"),
                category.get("name").alias("categoryName"),
                category.get("iconKey").alias("iconKey")
        );

        List<Predicate> predicates = this.setUpcomingTransactionsPredicates(
                filterParams.getRange(),
                filterParams.getAccountIds(),
                account,
                user,
                root,
                cb,
                userId
        );

        cq.orderBy(cb.asc(root.get("nextOccurrence")));
        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        int limit = paginationParams.getLimit();
        int offset = (paginationParams.getPage() - 1) * limit;

        return em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @param filterParams
     * @return
     */
    @Override
    public Long getUpcomingTransactionsCount(UpcomingTransactionFilterParams filterParams, Long userId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RecurringTransaction> root = countQuery.from(RecurringTransaction.class);
        Join<RecurringTransaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);

        List<Predicate> predicates = this.setUpcomingTransactionsPredicates(
                filterParams.getRange(),
                filterParams.getAccountIds(),
                account,
                user,
                root,
                cb,
                userId
        );

        countQuery.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(countQuery)
                .getSingleResult();
    }

    @Override
    public void reassignCategoryForUser(Long userId, Long oldCategoryId, Long newCategoryId) {
        em.createQuery(
                "UPDATE RecurringTransaction r " +
                        "SET r.category.id = :newCategoryId " +
                        "WHERE r.category.id = :oldCategoryId AND r.account.user.id = :userId")
                .setParameter("userId", userId)
                .setParameter("oldCategoryId", oldCategoryId)
                .setParameter("newCategoryId", newCategoryId)
                .executeUpdate();
    }

    @Override
    public void activateAllTransactionsByAccount(Long accountId, Long userId) {
        em.createQuery("UPDATE RecurringTransaction r SET r.isActive = TRUE WHERE account.id = :accountId AND account.user.id = :userId")
                .setParameter("accountId", accountId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void activateAllTransactionsByUser(Long userId) {
        em.createQuery("UPDATE RecurringTransaction r SET r.isActive = TRUE WHERE account.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deactivateAllTransactionsByAccount(Long accountId, Long userId) {
        em.createQuery("UPDATE RecurringTransaction r SET r.isActive = FALSE WHERE account.id = :accountId AND account.user.id = :userId")
                .setParameter("accountId", accountId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deactivateAllTransactionsByUser(Long userId) {
        em.createQuery("UPDATE RecurringTransaction r SET r.isActive = FALSE WHERE account.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deleteAllByAccount(Long accountId, Long userId) {
        em.createQuery("DELETE FROM RecurringTransaction r WHERE r.account.id = :accountId AND account.user.id = :userId")
                .setParameter("accountId", accountId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deleteAllByUser(Long userId) {
        em.createQuery("DELETE FROM RecurringTransaction r WHERE r.account.user.id =  :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public boolean existsByCategoryIdAndUserId(Long categoryId, Long userId) {
        List<Long> result = em.createQuery(
                        "SELECT t.id FROM RecurringTransaction t " +
                                "WHERE t.category.id = :categoryId AND t.account.user.id = :userId",
                        Long.class)
                .setParameter("categoryId", categoryId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return !result.isEmpty();
    }

    private List<Predicate> setUpcomingTransactionsPredicates(
            UpcomingTransactionsTimeRange range,
            List<Long> accountIds,
            Join<RecurringTransaction, Account> account,
            Join<Account, User> user,
            Root<RecurringTransaction> root,
            CriteriaBuilder cb,
            Long userId
    ) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(user.get("id"), userId));

        if (!accountIds.isEmpty()) {
            predicates.add(account.get("id").in(accountIds));
        }

        predicates.add(cb.equal(root.get("isActive"), true));

        if (range == null) {
            LocalDate defaultEndDate = UpcomingTransactionsTimeRange.NEXT_7_DAYS.calculateEndDate(LocalDate.now());
            predicates.add(cb.lessThanOrEqualTo(root.get("nextOccurrence"), defaultEndDate));
        } else {
            predicates.add(cb.lessThanOrEqualTo(root.get("nextOccurrence"), range.calculateEndDate(LocalDate.now())));
        }
        predicates.add(cb.greaterThanOrEqualTo(root.get("nextOccurrence"), LocalDate.now()));

        return predicates;
    }
}
