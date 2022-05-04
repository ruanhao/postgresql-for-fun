package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.Account;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends BaseRepository<Account, Long> {
    Account findByName(String name);

    @Query("update Account set balance = :balance where name = :name")
    @Modifying
    @Transactional
    void updateBalanceByName(String name, long balance);

    @Query(value = "INSERT INTO accounts(name, balance) VALUES(:name, :balance)", nativeQuery = true)
    @Modifying
    @Transactional
    void createOne(String name, long balance);

}
