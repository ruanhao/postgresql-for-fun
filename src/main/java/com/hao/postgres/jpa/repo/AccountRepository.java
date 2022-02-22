package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.Account;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends BaseRepository<Account, Long> {
    Account findByName(String name);
}
