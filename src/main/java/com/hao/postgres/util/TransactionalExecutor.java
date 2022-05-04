package com.hao.postgres.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalExecutor {

    @Transactional
    public void required(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.NEVER)
    public void never(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupported(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void supports(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nested(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatory(final Runnable runnable) {
        runnable.run();
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void readCommitted(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void readUncommitted(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableRead(final Runnable runnable) {
        runnable.run();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void serializable(final Runnable runnable) {
        runnable.run();
    }
}
