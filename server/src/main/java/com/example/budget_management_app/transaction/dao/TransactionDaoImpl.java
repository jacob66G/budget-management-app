package com.example.budget_management_app.transaction.dao;


import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.TransactionFilterParams;
import com.example.budget_management_app.transaction.dto.TransactionPaginationParams;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_receipts.domain.TransactionPhoto;
import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionDaoImpl implements TransactionDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * @return List of TransactionView objects
     */
    @Override
    public List<Tuple> getTuples(
            TransactionPaginationParams paginationParams,
            TransactionFilterParams filterParams,
            Long userId
                                       ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Transaction> root = cq.from(Transaction.class);
        Join<Transaction, Category> category = root.join("category", JoinType.INNER);
        Join<Transaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);
        Join<Transaction, RecurringTransaction> recTransaction = root.join("recurringTransaction", JoinType.LEFT);
        Join<Transaction, TransactionPhoto> transactionPhoto = root.join("transactionPhoto", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("transactionId"),
                root.get("title").alias("title"),
                root.get("amount").alias("amount"),
                root.get("type").alias("type"),
                root.get("description").alias("description"),
                root.get("transactionDate").alias("transactionDate"),
                account.get("id").alias("accountId"),
                account.get("name").alias("accountName"),
                account.get("currency").alias("currency"),
                category.get("id").alias("categoryId"),
                category.get("name").alias("categoryName"),
                category.get("iconKey").alias("iconKey"),
                recTransaction.get("id").alias("recId"),
                transactionPhoto.get("id").alias("transactionPhotoId")
        );

        List<Predicate> predicates = this.setPredicates(
                account,
                category,
                recTransaction,
                user,
                root,
                cb,
                filterParams,
                userId
        );

        SortedBy sortedBy = paginationParams.getSortedBy();
        SortDirection sortDirection = paginationParams.getSortDirection();
        Order order;
        if (sortedBy.equals(SortedBy.AMOUNT)) {
            if (sortDirection.equals(SortDirection.ASC)) {
                order = cb.asc(root.get("amount"));
            } else {
                order = cb.desc(root.get("amount"));
            }
        } else if (sortedBy.equals(SortedBy.CATEGORY)) {
            if (sortDirection.equals(SortDirection.ASC)) {
                order = cb.asc(category.get("name"));
            } else {
                order = cb.desc(category.get("name"));
            }
        } else {
            if (sortDirection.equals(SortDirection.ASC)) {
                order = cb.asc(root.get("transactionDate"));
            } else {
                order = cb.desc(root.get("transactionDate"));
            }
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(order);

        int limit = paginationParams.getLimit();
        int offset = (paginationParams.getPage() - 1) * limit;

        return em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @return the number of transactions
     */
    @Override
    public Long getCount(TransactionFilterParams filterParams, Long userId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Transaction> root = countQuery.from(Transaction.class);
        Join<Transaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);
        Join<Transaction, Category> category = root.join("category", JoinType.INNER);
        Join<Transaction, RecurringTransaction> recTransaction = root.join("recurringTransaction", JoinType.LEFT);

        List<Predicate> predicates = this.setPredicates(
                account,
                category,
                recTransaction,
                user,
                root,
                cb,
                filterParams,
                userId
        );

        countQuery.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(countQuery)
                .getSingleResult();
    }

    @Override
    public Transaction save(Transaction transaction) {
        em.persist(transaction);
        em.flush();
        return transaction;
    }

    @Override
    public void delete(Transaction transaction) {
        em.remove(transaction);
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(Long id, Long userId) {

        List<Transaction> results = em.createQuery("""
                        SELECT t FROM Transaction t
                        JOIN t.account a
                        WHERE t.id = :id AND a.user.id = :userId
                        """, Transaction.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return results.stream().findFirst();
    }

    @Override
    public Optional<Transaction> findByIdAndUserIdAndCategoryId(Long id, Long categoryId, Long userId) {

        List<Transaction> results = em.createQuery("""
                        SELECT t FROM Transaction t
                        JOIN t.category c
                        WHERE t.id = :id AND c.id = :categoryId AND c.user.id = :userId
                        """, Transaction.class)
                .setParameter("id", id)
                .setParameter("categoryId", categoryId)
                .setParameter("userId", userId)
                .getResultList();

        return results.stream().findFirst();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public List<Transaction> findByRecurringTransactionId(Long id) {

        return em.createQuery("""
                        SELECT t FROM Transaction t
                        WHERE t.recurringTransaction.id = :recurringTransactionId
                        """, Transaction.class)
                .setParameter("recurringTransactionId", id)
                .getResultList();
    }

    /**
     * @param id
     * @param userId
     * @return
     */
    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        Long count = em.createQuery("""
                    SELECT COUNT (t) FROM Transaction t
                    WHERE t.id = :id
                    AND
                    t.account.user.id = :userId
                """, Long.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean existsByCategoryIdAndUserId(Long categoryId, Long userId) {
        List<Long> result = em.createQuery(
                        "SELECT t.id FROM Transaction t " +
                                "WHERE t.category.id = :categoryId AND t.account.user.id = :userId",
                        Long.class)
                .setParameter("categoryId", categoryId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return !result.isEmpty();
    }

    @Override
    public boolean existsByAccountIdAndUserId(Long accountId, Long userId) {
        List<Long> result = em.createQuery(
                        "SELECT t.id FROM Transaction t " +
                                "WHERE t.account.id = :accountId AND t.account.user.id = :userId",
                        Long.class)
                .setParameter("accountId", accountId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return !result.isEmpty();
    }

    @Override
    public void reassignCategoryForUser(Long userId, Long oldCategoryId, Long newCategoryId) {
        em.createQuery(
                        "UPDATE Transaction t " +
                                "SET t.category.id = :newCategoryId " +
                                "WHERE t.category.id = :oldCategoryId AND t.account.user.id = :userId")
                .setParameter("userId", userId)
                .setParameter("oldCategoryId", oldCategoryId)
                .setParameter("newCategoryId", newCategoryId)
                .executeUpdate();
    }

    @Override
    public void deleteAllByAccount(Long accountId, Long userId) {
        em.createQuery("DELETE FROM Transaction t WHERE t.account.id = :accountId AND t.account.user.id = :userId")
                .setParameter("accountId", accountId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deleteAllByUser(Long userId) {
        em.createQuery("DELETE FROM Transaction t WHERE t.account.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * @param startDate
     * @param endDate
     * @param accountName
     * @param userId
     * @return
     */
    @Override
    public List<Tuple> getUserExpensesByAccount(LocalDate startDate, LocalDate endDate, String accountName, Long userId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Transaction> root = cq.from(Transaction.class);
        Join<Transaction, Category> category = root.join("category", JoinType.INNER);
        Join<Transaction, Account> account = root.join("account", JoinType.INNER);
        Join<Account, User> user = account.join("user", JoinType.INNER);

        cq.multiselect(
                root.get("title").alias("title"),
                root.get("amount").alias("amount"),
                category.get("name").alias("categoryName")
        );

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("type"), TransactionType.EXPENSE));
        predicates.add(cb.equal(user.get("id"), userId));
        predicates.add(cb.equal(account.get("name"), accountName));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(root.get("transactionDate")));

        return em.createQuery(cq)
                .getResultList();
    }

    @Override
    public BigDecimal getSumForAccountInPeriod(Long accountId, LocalDateTime start, LocalDateTime end, TransactionType type) {
        return em.createQuery("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                "WHERE t.account.id = :accountId AND t.type = :type AND t.transactionDate BETWEEN :from AND :to", BigDecimal.class)
                .setParameter("accountId", accountId)
                .setParameter("from", start)
                .setParameter("to", end)
                .setParameter("type", type)
                .getSingleResult();
    }

    private List<Predicate> setPredicates(
                                Join<Transaction, Account> account,
                                Join<Transaction, Category> category,
                                Join<Transaction, RecurringTransaction> recTransaction,
                                Join<Account, User> user,
                                Root<Transaction> root,
                                CriteriaBuilder cb,
                                TransactionFilterParams filterParams,
                                Long userId) {

        List<Predicate> predicates = new ArrayList<>();

        TransactionTypeFilter type = filterParams.getType();
        TransactionModeFilter mode = filterParams.getMode();
        List<Long> accountIds = filterParams.getAccountIds();
        List<Long> categoryIds = filterParams.getCategoryIds();
        LocalDate since = filterParams.getSince();
        LocalDate to = filterParams.getTo();

        predicates.add(cb.equal(user.get("id"), userId));

        if (!type.equals(TransactionTypeFilter.ALL)) {
            predicates.add(cb.equal(cb.lower(root.get("type")), type.toString().toLowerCase()));
        }
        if (!mode.equals(TransactionModeFilter.ALL)) {
            if (mode.equals(TransactionModeFilter.REGULAR)) {
                predicates.add(cb.isNull(recTransaction.get("id")));
            } else {
                predicates.add(cb.isNotNull(recTransaction.get("id")));
            }
        }
        if (!accountIds.isEmpty()) {
            predicates.add(account.get("id").in(accountIds));
        }
        if (!categoryIds.isEmpty()) {
            predicates.add(category.get("id").in(categoryIds));
        }
        if (since != null) {
            LocalDateTime startDate = since.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        }
        if (to != null) {
            LocalDateTime endDate = to.plusDays(1).atStartOfDay();
            predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        }

        return predicates;
    }
}
