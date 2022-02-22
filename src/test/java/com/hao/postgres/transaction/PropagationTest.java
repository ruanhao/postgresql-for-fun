package com.hao.postgres.transaction;

import com.hao.postgres.jpa.repo.AccountRepository;
import com.hao.postgres.util.TransactionalExecutor;
import java.util.Arrays;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.UnexpectedRollbackException;

@SpringBootTest
@Sql("/sql/accounts.sql")
@Slf4j
public class PropagationTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionalExecutor transactionalExecutor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    private static final int ORIGIN = 1000;
    private static final int AMOUNT = 100;

//    @BeforeAll
//    @Sql("/sql/accounts.sql")
//    public static void beforeAll() {}

    /**
     * 要使用基于注解的 Spring 事务，必须要有 transactionManager 和 AOP 的支持
     */
    @Test
    public void mustHaveTransactionManager() {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).anyMatch(name -> Objects.equals("transactionManager", name));
        // Arrays.stream(beanDefinitionNames).anyMatch(name -> Objects.equals("transactionTemplate", name));
    }

    // ========== 不想要事务功能 (never, not_supported) ==========

    /**
     * Propagation.NEVER 不能在事务环境中运行，否则报 IllegalTransactionStateException 异常
     */
    @Test
    public void propagationNever() {
        // CASE 1
        never(() -> { // 没有事务的环境下正常运行
            withdraw("Mike", AMOUNT);
            Assertions.assertEquals(1000 - AMOUNT, getBalanceWithoutL1Cache("Mike"));
            deposit("Mike", AMOUNT);
            Assertions.assertEquals(1000, getBalanceWithoutL1Cache("Mike"));
        });

        // CASE 2
        try {
            required(() -> {
                withdraw("Mike", AMOUNT);
                Assertions.assertEquals(1000 - AMOUNT, getBalanceWithoutL1Cache("Mike"));
                never(() -> log.info("执行到这里之前就已报错"));
            });
        } catch (Exception e) {
            // e.printStackTrace();
            Assertions.assertTrue(e instanceof IllegalTransactionStateException);
            Assertions.assertEquals("Existing transaction found for transaction marked with propagation 'never'", e.getMessage());
        }
        Assertions.assertEquals(1000, getBalanceWithoutL1Cache("Mike"), "Should rollback");
    }



    /**
     * Propagation.NOT_SUPPORTED 即使在事务环境中运行，也不支持回滚
     */
    @Test
    public void propagationNotSupported() {
        try {
            required(() -> {
                withdraw("Mike", AMOUNT);
                notSupported(() -> {
                    deposit("Jack", AMOUNT);
                    throw new RuntimeException("Inner");
                });
            });
        } catch (Exception e) {
           // e.printStackTrace();
            Assertions.assertEquals("Inner", e.getMessage());
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Mike"), "Should rollback");
            Assertions.assertEquals(ORIGIN + AMOUNT, getBalanceWithoutL1Cache("Jack"), "Did not rollback");
        }
    }

    // ========== 事务功能可有可无 (supports) ==========

    /**
     * 处于事务环境就支持回滚，否则就不回滚
     */
    @Test
    public void propagationSupports() {
        // CASE 1
        try {
            required(() -> {
                withdraw("Mike", AMOUNT);
                supports(() -> deposit("Jack", AMOUNT)); // run in the existing transaction
                throw new RuntimeException("Outer");
            });
        } catch (Exception e) {
            // e.printStackTrace();
            Assertions.assertEquals("Outer", e.getMessage());
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Mike"), "Should rollback");
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Jack"), "Should rollback");
        }

        // CASE 2
        try {
            supports(() -> {
                deposit("Jack", AMOUNT); // no transaction available
                throw new RuntimeException("Inner");
            });
        } catch (Exception e) {
            // e.printStackTrace();
            Assertions.assertEquals("Inner", e.getMessage());
            Assertions.assertEquals(ORIGIN + AMOUNT, getBalanceWithoutL1Cache("Jack"), "Did not rollback");
        }
    }


    // ========== 想要支持事务功能 (required, requires_new, nested, mandatory) ==========

    /**
     * Propagation.REQUIRED: 如果处于事务的环境中，则共用，否则新建
     */
    @Test
    public void propagationRequired() {
        try {
            required(() -> {
                withdraw("Mike", AMOUNT);
                try {
                    required(() -> {
                        deposit("Jack", AMOUNT);
                        throw new RuntimeException("Inner");
                    });
                } catch (Exception e) {}
            });
        } catch (Exception e) {
            assert e instanceof UnexpectedRollbackException;
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Jack"), "Should rollback");
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Mike"), "Should rollback");
        }
    }

    /**
     * Propagation.REQUIRES_NEW: 始终新建一个事务，无论是否处于事务的环境
     */
    @Test
    public void propagationRequiresNew() {
        try {
            required(() -> {
                withdraw("Mike", AMOUNT);
                requiresNew(() -> deposit("Jack", AMOUNT)); // run in a NEW transaction
                throw new RuntimeException("Outer");
            });
        } catch (Exception e) {
            // e.printStackTrace();
            Assertions.assertEquals("Outer", e.getMessage());
            Assertions.assertEquals(ORIGIN, getBalanceWithoutL1Cache("Mike"), "Should rollback");
            Assertions.assertEquals(ORIGIN + AMOUNT, getBalanceWithoutL1Cache("Jack"), "Already commit");
        }
    }

    /**
     * Propagation.NESTED: 如果没有处于事务的环境，则新建事务，否则创建一个 SAVEPOINT （而不是新建一个事务） <br/>
     * 参考：https://www.jianshu.com/p/d7c17684f801
     */
    @Test
    public void propagationNested() {
        try {
            required(() -> {
                nested(() -> deposit("Jack", AMOUNT)); // PostgreSQL does not support SAVEPOINT
            });
        } catch (Exception e) {
            // e.printStackTrace();
            assert e instanceof NestedTransactionNotSupportedException;
        }
    }

    /**
     * Propagation.MANDATORY: 当前必须处于事务环境，否则报错
     */
    @Test
    public void propagationMandatory() {
        // With transaction
        required(() -> mandatory(() -> deposit("Jack", AMOUNT)));

        // Without transaction
        try {
            mandatory(() -> deposit("Jack", AMOUNT));
        } catch (Exception e) {
            // e.printStackTrace();
            assert e instanceof IllegalTransactionStateException;
            Assertions.assertEquals("No existing transaction found for transaction marked with propagation 'mandatory'", e.getMessage());
        }

    }




    private long getBalanceWithoutL1Cache(String name) {
        em.clear(); // 清除 Hibernate L1 缓存
        return accountRepository.findByName(name).getBalance();
    }

    private void required(Runnable runnable) {
        transactionalExecutor.required(runnable);
    }

    private void never(Runnable runnable) {
        transactionalExecutor.never(runnable);
    }

    private void notSupported(Runnable runnable) {
        transactionalExecutor.notSupported(runnable);
    }

    private void supports(Runnable runnable) {
        transactionalExecutor.supports(runnable);
    }

    private void requiresNew(Runnable runnable) {
        transactionalExecutor.requiresNew(runnable);
    }

    private void nested(Runnable runnable) {
        transactionalExecutor.nested(runnable);
    }

    private void mandatory(Runnable runnable) {
        transactionalExecutor.mandatory(runnable);
    }

    private void withdraw(String name, long amount) {
        var sql = "update accounts set balance = balance - ? where name = ?";
        jdbcTemplate.update(sql, amount, name);
    }

    private void deposit(String name, long amount) {
        var sql = "update accounts set balance = balance + ? where name = ?";
        jdbcTemplate.update(sql, amount, name);
    }
}
