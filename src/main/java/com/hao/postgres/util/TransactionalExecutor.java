package com.hao.postgres.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalExecutor {

    @Transactional(propagation = Propagation.REQUIRED)
    public void required(final Runnable runnable) {
        runnable.run();
    }
}
