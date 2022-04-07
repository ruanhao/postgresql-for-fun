package com.hao.postgres.jpa.entity;

public interface TenantAware {
    void setTenantId(String tenantId);
}