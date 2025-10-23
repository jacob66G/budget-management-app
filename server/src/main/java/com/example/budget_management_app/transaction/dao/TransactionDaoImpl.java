package com.example.budget_management_app.transaction.dao;


import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.transaction.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionDaoImpl implements TransactionDao{

    @PersistenceContext
    private EntityManager em;
    /**
     * @return List of TransactionView objects
     */
    @Transactional(readOnly = true)
    @Override
    public List<Tuple> getTransactions(int page,
                                       int limit,
                                       TransactionTypeFilter type,
                                       TransactionModeFilter mode,
                                       List<Long> accounts,
                                       LocalDate since,
                                       LocalDate to,
                                       SortedBy sortedBy,
                                       SortDirection sortedType
                                       ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Transaction> root = cq.from(Transaction.class);
        Join<Transaction, Category> category = root.join("category", JoinType.INNER);
        Join<Transaction, Account> account = root.join("account", JoinType.INNER);
        Join<Transaction, RecurringTransaction> recTransaction = root.join("recurringTransaction", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("transactionId"),
                root.get("amount").alias("amount"),
                root.get("type").alias("type"),
                root.get("description").alias("description"),
                root.get("transactionDate").alias("transactionDate"),
                account.get("id").alias("accountId"),
                account.get("name").alias("accountName"),
                category.get("id").alias("categoryId"),
                category.get("name").alias("categoryName"),
                category.get("iconPath").alias("iconPath"),
                recTransaction.get("id").alias("recId")
        );

        List<Predicate> predicates = this.setPredicates(
                account,
                recTransaction,
                root,
                cb,
                type,
                mode,
                accounts,
                since,
                to
                );

        Order order;
        if (sortedBy.equals(SortedBy.AMOUNT)) {
            if (sortedType.equals(SortDirection.ASC)) {
                order = cb.asc(root.get("amount"));
            } else {
                order = cb.desc(root.get("amount"));
            }
        } else if (sortedBy.equals(SortedBy.CATEGORY)) {
            if (sortedType.equals(SortDirection.ASC)) {
                order = cb.asc(category.get("name"));
            } else {
                order = cb.desc(category.get("name"));
            }
        } else {
            if (sortedType.equals(SortDirection.ASC)) {
                order = cb.asc(root.get("transactionDate"));
            } else {
                order = cb.desc(root.get("transactionDate"));
            }
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(order);

        int offset = (page - 1) * limit;

        return em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @return the number of transactions
     */
    @Transactional(readOnly = true)
    @Override
    public Long getTransactionsCount(TransactionTypeFilter type,
                                     TransactionModeFilter mode,
                                     List<Long> accounts,
                                     LocalDate since,
                                     LocalDate to) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Transaction> root = countQuery.from(Transaction.class);
        Join<Transaction, Account> account = root.join("account", JoinType.INNER);
        Join<Transaction, RecurringTransaction> recTransaction = root.join("recurringTransaction", JoinType.LEFT);

        List<Predicate> predicates = this.setPredicates(
                account,
                recTransaction,
                root,
                cb,
                type,
                mode,
                accounts,
                since,
                to);

        countQuery.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(countQuery)
                .getSingleResult();
    }

    @Transactional
    @Override
    public Transaction saveTransaction(Transaction transaction) {
        em.persist(transaction);
        em.flush();
        return transaction;
    }

    private List<Predicate> setPredicates(
                                Join<Transaction, Account> account,
                                Join<Transaction, RecurringTransaction> recTransaction,
                                Root<Transaction> root,
                                CriteriaBuilder cb,
                                TransactionTypeFilter type,
                                TransactionModeFilter mode,
                                List<Long> accounts,
                                LocalDate since,
                                LocalDate to) {

        List<Predicate> predicates = new ArrayList<>();

        if (!type.equals(TransactionTypeFilter.ALL)) {
            predicates.add(cb.equal(cb.lower(root.get("type")),  type.toString().toLowerCase()));
        }
        if (!mode.equals(TransactionModeFilter.ALL)) {
            if (mode.equals(TransactionModeFilter.REGULAR)) {
                predicates.add(cb.isNull(recTransaction.get("id")));
            } else {
                predicates.add(cb.isNotNull(recTransaction.get("id")));
            }
        }
        if (!accounts.isEmpty()) {
            predicates.add(account.get("id").in(accounts));
        }

        if (since != null) {
            LocalDateTime startDate = since.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        }
        if (to != null) {
            LocalDateTime endDate = to.atStartOfDay();
            predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        }

        return predicates;
    }
}
