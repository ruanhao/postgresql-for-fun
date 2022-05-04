package com.hao.postgres.transaction;

import com.hao.postgres.jpa.entity.Account;
import com.hao.postgres.jpa.entity.Ball;
import com.hao.postgres.jpa.repo.AccountRepository;
import com.hao.postgres.util.CommandRunner;
import com.hao.postgres.util.JDBCUtils;
import com.hao.postgres.util.TransactionalExecutor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/accounts.sql")

@Slf4j
public class IsolationTest {

    private static final ThreadPoolExecutor THREAD_POOL =
            new ThreadPoolExecutor(
                    1, 8,
                    1000L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(16),
                    new BasicThreadFactory.Builder().namingPattern("isolation-test-%d").daemon(true).build()
            );

    @Autowired
    CommandRunner commandRunner;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionalExecutor transactionalExecutor;

    @Autowired
    JDBCUtils jdbcUtils;

    Exception serializableException;

    Exception readCommittedException;

    @Test
    public void showDefaultIsolationLevel() {
        System.out.println(commandRunner.psql("SHOW default_transaction_isolation;"));
    }

    @BeforeEach
    public void beforeEach() {
        serializableException = null;
        readCommittedException = null;
    }

    private void spawnReadCommitted(Runnable runnable, String threadName) {
        spawn(() -> {
            try {
                transactionalExecutor.readCommitted(runnable);
            } catch (Exception e) {
                readCommittedException = e;
            }
        }, threadName);
    }

    private void spawnSerializable(Runnable runnable, String threadName) {
        spawn(() -> {
            try {
                transactionalExecutor.serializable(runnable);
            } catch (Exception e) {
                serializableException = e;
            }
        }, threadName);
    }

    private void spawnRepeatableRead(Runnable runnable, String threadName) {
        spawn(() -> transactionalExecutor.repeatableRead(runnable), threadName);
    }

    private void spawn(Runnable runnable, String name) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(false);
        thread.setName(name);
        thread.start();
        // THREAD_POOL.submit(runnable);
    }

    @SneakyThrows
    void sleep(long time, TimeUnit unit) {
        unit.sleep(time);
    }

    @Test
    @SneakyThrows
    public void readCommitted_NonrepeatableRead() {
        final String name = "readCommitted_NonrepeatableRead";
        accountRepository.save(Account.builder().name(name).balance(1000L).build());
        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(1);
        CountDownLatch mainThreadToGo = new CountDownLatch(1);

        List<Long> balanceCollector = new ArrayList<>();
        List<Long> resultCollector = new ArrayList<>();

        spawnReadCommitted(() -> {
            String sql = String.format("select * from accounts where name = '%s';", name);
            log.info("Getting balance #1");
            resultCollector.add(jdbcUtils.findOne(sql, Account.class).getBalance());
            balanceCollector.add(accountRepository.findByName(name).getBalance());
            mainThreadToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Get balance #2");
            balanceCollector.add(accountRepository.findByName(name).getBalance());
            resultCollector.add(jdbcUtils.findOne(sql, Account.class).getBalance());
            waitForComplete.countDown();
        }, "tx-1");

        mainThreadToGo.await();
        accountRepository.updateBalanceByName(name, 2000L);
        tx1ToGo.countDown();
        waitForComplete.await();

        // System.err.println("balanceCollector: " + balanceCollector);
        Assertions.assertTrue(balanceCollector.size() == 2);
        Assertions.assertTrue(new HashSet<>(balanceCollector).size() == 1); // due to hibernate cache thing

        Assertions.assertTrue(resultCollector.size() == 2);
        Assertions.assertTrue(new HashSet<>(resultCollector).size() == 2);
    }

    @Test
    @SneakyThrows
    public void readCommitted_PhantomRead() {
        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(1);
        CountDownLatch mainThreadToGo = new CountDownLatch(1);

        List<Integer> countCollector = new ArrayList<>();
        List<Integer> resultCollector = new ArrayList<>();

        spawnReadCommitted(() -> {
            String sql = "select * from accounts;";
            log.info("Getting balance #1");
            resultCollector.add(jdbcUtils.findMulti(sql, Account.class).size());
            countCollector.add(accountRepository.findAll().size());
            mainThreadToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Get balance #2");
            countCollector.add(accountRepository.findAll().size());
            resultCollector.add(jdbcUtils.findMulti(sql, Account.class).size());
            waitForComplete.countDown();
        }, "tx-1");

        mainThreadToGo.await();
        // accountRepository.save(Account.builder().name(UUID.randomUUID().toString()).balance(5000L).build());
        accountRepository.createOne(UUID.randomUUID().toString(), 5000L);
        tx1ToGo.countDown();
        waitForComplete.await();

        System.err.println("countCollector: " + countCollector);
        System.err.println("resultCollector: " + resultCollector);

        Assertions.assertTrue(countCollector.get(0) + 1 == countCollector.get(1));
        Assertions.assertTrue(resultCollector.get(0) + 1 == resultCollector.get(1));

    }


    /**
     * https://www.cnblogs.com/ivan-uno/p/8274355.html
     */
    @Test
    @SneakyThrows
    @Sql("/sql/balls.sql")
    public void readCommitted_Serialization_Anomaly() {

        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(2);
        CountDownLatch tx2ToGo = new CountDownLatch(1);

        spawnReadCommitted(() -> {
            log.info("Setting White to Black");
            jdbcUtils.execute("update balls set color = 'Black' where color = 'White'");
            tx2ToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitForComplete.countDown();
        }, "tx-1");

        spawnReadCommitted(() -> {
            try {
                tx2ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Setting Black to White");
            jdbcUtils.execute("update balls set color = 'White' where color = 'Black'");
            tx1ToGo.countDown();
            waitForComplete.countDown();
        }, "tx-2");


        waitForComplete.await();
        sleep(2L, TimeUnit.SECONDS);

        Assertions.assertNull(readCommittedException);
        List<Ball> balls = jdbcUtils.findMulti("select * from balls;", Ball.class);
        Assertions.assertEquals(2, balls.stream().filter(b -> b.getColor().equals("White")).count());
        Assertions.assertEquals(2, balls.stream().filter(b -> b.getColor().equals("Black")).count());

    }

    @Test
    @SneakyThrows
    @Sql("/sql/balls.sql")
    public void serializable_Serialization_Anomaly_Not_Possible() {

        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(2);
        CountDownLatch tx2ToGo = new CountDownLatch(1);

        spawnSerializable(() -> {
            log.info("Setting White to Black");
            jdbcUtils.execute("update balls set color = 'Black' where color = 'White'");
            tx2ToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitForComplete.countDown();
        }, "tx-1");

        spawnSerializable(() -> {
            try {
                tx2ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Setting Black to White");
            jdbcUtils.execute("update balls set color = 'White' where color = 'Black'");
            tx1ToGo.countDown();
            waitForComplete.countDown();
        }, "tx-2");


        waitForComplete.await();
        sleep(2L, TimeUnit.SECONDS);
        System.err.println("ex: " + serializableException);
        Assertions.assertTrue(serializableException instanceof JpaSystemException);
        List<Ball> balls = jdbcUtils.findMulti("select * from balls;", Ball.class);
        boolean allWhite = balls.stream().allMatch(b -> b.getColor().equals("White"));
        boolean allBlack = balls.stream().allMatch(b -> b.getColor().equals("Black"));
        Assertions.assertTrue(allWhite || allBlack); // only one tx succeeds

    }


    @Test
    @SneakyThrows
    public void repeatableRead_PhantomRead_Not_In_PG() {
        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(1);
        CountDownLatch mainThreadToGo = new CountDownLatch(1);

        List<Integer> countCollector = new ArrayList<>();
        List<Integer> resultCollector = new ArrayList<>();

        spawnRepeatableRead(() -> {
            String sql = "select * from accounts;";
            log.info("Getting balance #1");
            resultCollector.add(jdbcUtils.findMulti(sql, Account.class).size());
            countCollector.add(accountRepository.findAll().size());
            mainThreadToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Get balance #2");
            countCollector.add(accountRepository.findAll().size());
            resultCollector.add(jdbcUtils.findMulti(sql, Account.class).size());
            waitForComplete.countDown();
        }, "tx-1");

        mainThreadToGo.await();
        // accountRepository.save(Account.builder().name(UUID.randomUUID().toString()).balance(5000L).build());
        accountRepository.createOne(UUID.randomUUID().toString(), 5000L);
        tx1ToGo.countDown();
        waitForComplete.await();

        System.err.println("countCollector: " + countCollector);
        System.err.println("resultCollector: " + resultCollector);

        Assertions.assertTrue(countCollector.get(0) == countCollector.get(1));
        Assertions.assertTrue(resultCollector.get(0) == resultCollector.get(1));

    }

    @Test
    @SneakyThrows
    public void RepeatableRead_Not_Possible_NonrepeatableRead() {
        final String name = "RepeatableRead_Not_Possible_NonrepeatableRead";
        accountRepository.save(Account.builder().name(name).balance(1000L).build());
        CountDownLatch tx1ToGo = new CountDownLatch(1);
        CountDownLatch waitForComplete = new CountDownLatch(1);
        CountDownLatch mainThreadToGo = new CountDownLatch(1);

        List<Long> balanceCollector = new ArrayList<>();
        List<Long> resultCollector = new ArrayList<>();
        spawnRepeatableRead(() -> {
            String sql = String.format("select * from accounts where name = '%s';", name);
            log.info("Getting balance #1");
            resultCollector.add(jdbcUtils.findOne(sql, Account.class).getBalance());
            balanceCollector.add(accountRepository.findByName(name).getBalance());
            mainThreadToGo.countDown();
            try {
                tx1ToGo.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Getting balance #2");
            resultCollector.add(jdbcUtils.findOne(sql, Account.class).getBalance());
            balanceCollector.add(accountRepository.findByName(name).getBalance());
            waitForComplete.countDown();
        }, "tx-1");

        mainThreadToGo.await();
        accountRepository.updateBalanceByName(name, 2000L);
        tx1ToGo.countDown();
        waitForComplete.await();

        Assertions.assertTrue(balanceCollector.size() == 2);
        Assertions.assertTrue(new HashSet<>(balanceCollector).size() == 1); // due to hibernate cache thing

        Assertions.assertTrue(resultCollector.size() == 2);
        Assertions.assertTrue(new HashSet<>(resultCollector).size() == 1);
        Assertions.assertTrue(new ArrayList<>(resultCollector).get(0) == 1000L);

    }
}
