package com.example.budget_management_app.account.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.AccountSortableField;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.dto.SearchCriteria;
import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountDao {

    @PersistenceContext
    private EntityManager em;

    public Optional<Account> findByIdAndUser(Long id, Long userId) {
        List<Account> result = em.createQuery("SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId", Account.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.stream().findFirst();
    }

    public boolean areAccountsBelongToUser(Long userId, List<Long> accounts) {
        Long count = em.createQuery("""
            SELECT COUNT(a) FROM Account a
            WHERE a.id IN :accounts AND a.user.id = :userId
            """, Long.class)
                .setParameter("accounts", accounts)
                .setParameter("userId", userId)
                .getSingleResult();
        return count != null && count == accounts.size();
    }


    public List<Account> findByUserAndCriteria(Long userId, SearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);
        Join<Account, User> userJoin = root.join("user", JoinType.INNER);

        List<Predicate> predicates = preparePredicates(userId, criteria, cb, userJoin, root);

        cq.where(predicates.toArray(new Predicate[0]));
        Path<?> sortField = getSortField(criteria.sortBy(), root);
        Order order = getSortOrder(criteria.sortDirection(), cb, sortField);
        cq.orderBy(order);

        TypedQuery<Account> query = em.createQuery(cq);
        return query.getResultList();
    }

    public Account save(Account account) {
        em.persist(account);
        em.flush();
        return account;
    }

    public Account update(Account account) {
        return em.merge(account);
    }

    public boolean existsByNameAndUser(String name, Long userId, Long existingAccountId) {
        String query = "SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND LOWER(a.name) = LOWER(:name) AND " +
                "(:existingAccountId IS NULL OR a.id != :existingAccountId)";

        Long count = em.createQuery(query, Long.class)
                .setParameter("userId", userId)
                .setParameter("name", name)
                .setParameter("existingAccountId", existingAccountId)
                .getSingleResult();

        return count > 0;
    }

    public void activateAll(Long userId) {
        em.createQuery(
                        "UPDATE Account a SET a.accountStatus = :status, a.includeInTotalBalance = true " +
                                "WHERE a.user.id = :userId AND a.accountStatus != :status")
                .setParameter("status", AccountStatus.ACTIVE)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void deactivateAll(Long userId) {
        em.createQuery(
                        "UPDATE Account a SET a.accountStatus = :status, a.includeInTotalBalance = false " +
                                "WHERE a.user.id = :userId AND a.accountStatus != :status")
                .setParameter("status", AccountStatus.INACTIVE)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void delete(Account account) {
        em.remove(account);
    }

    public void deleteAll(Long userId) {
        em.createQuery("DELETE FROM Account a WHERE a.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private List<Predicate> preparePredicates(Long userId, SearchCriteria criteria, CriteriaBuilder cb, Join<Account, User> userJoin, Root<Account> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(userJoin.get("id"), userId));

        if (StringUtils.hasText(criteria.type())) {
            predicates.add(cb.equal(root.get("accountType"), criteria.type().toUpperCase()));
        }
        if (StringUtils.hasText(criteria.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.name().toLowerCase() + "%"));
        }
        if (criteria.status() != null && !criteria.status().isEmpty()) {
            predicates.add(root.get("accountStatus").in(criteria.status().stream().map(String::toUpperCase).toList()));
        }
        if (criteria.currencies() != null && !criteria.currencies().isEmpty()) {
            predicates.add(root.get("currency").in(criteria.currencies().stream().map(String::toUpperCase).toList()));
        }
        if (criteria.budgetTypes() != null && !criteria.budgetTypes().isEmpty()) {
            predicates.add(root.get("budgetType").in(criteria.budgetTypes().stream().map(String::toUpperCase).toList()));
        }
        if (criteria.minBalance() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("balance"), criteria.minBalance()));
        }
        if (criteria.maxBalance() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("balance"), criteria.maxBalance()));
        }
        if (criteria.minTotalIncome() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalIncome"), criteria.minTotalIncome()));
        }
        if (criteria.maxTotalIncome() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalIncome"), criteria.maxTotalIncome()));
        }
        if (criteria.minTotalExpense() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalExpense"), criteria.minTotalExpense()));
        }
        if (criteria.maxTotalExpense() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalExpense"), criteria.maxTotalExpense()));
        }
        if (criteria.minBudget() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("budget"), criteria.minBudget()));
        }
        if (criteria.maxBudget() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("budget"), criteria.maxBudget()));
        }
        if (criteria.includedInTotalBalance() != null) {
            predicates.add(cb.equal(root.get("includeInTotalBalance"), criteria.includedInTotalBalance()));
        }
        if (criteria.createdBefore() != null) {
            predicates.add(cb.lessThan(root.get("createdAt"), criteria.createdBefore()));
        }
        if (criteria.createdAfter() != null) {
            predicates.add(cb.greaterThan(root.get("createdAt"), criteria.createdAfter()));
        }

        return predicates;
    }

    private Path<?> getSortField(String sortBy, Root<Account> root) {
        if (!StringUtils.hasText(sortBy)) {
            return root.get("createdAt");
        }
        try {
            AccountSortableField.valueOf(sortBy.toUpperCase());
            return root.get(sortBy);
        } catch (IllegalArgumentException e) {
            return root.get("createdAt");
        }
    }

    private Order getSortOrder(String sortDirection, CriteriaBuilder cb, Path<?> sortField) {
        boolean asc = "ASC".equalsIgnoreCase(sortDirection);
        return asc ? cb.asc(sortField) : cb.desc(sortField);
    }
}
