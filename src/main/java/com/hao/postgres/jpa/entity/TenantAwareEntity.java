package com.hao.postgres.jpa.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@RequiredArgsConstructor
@Getter
@Setter
@MappedSuperclass()
@FilterDef(
    name = TenantAwareEntity.TENANT_FILTER_NAME,
    parameters = @ParamDef(name = TenantAwareEntity.TENANT_FILTER_ARGUMENT_NAME, type = "string"),
    defaultCondition =
        TenantAwareEntity.TENANT_ID_PROPERTY_NAME + "= :" + TenantAwareEntity.TENANT_FILTER_ARGUMENT_NAME)
@Filter(name = TenantAwareEntity.TENANT_FILTER_NAME)
public class TenantAwareEntity { // https://github.com/M-Devloo/Spring-boot-auth0-discriminator-multitenancy
  public static final String TENANT_FILTER_NAME = "tenantFilter";
  public static final String TENANT_FILTER_ARGUMENT_NAME = "tenantId";
  static final String TENANT_ID_PROPERTY_NAME = "tenant_id";

  @Column(name = TENANT_ID_PROPERTY_NAME, nullable = false)
  String tenantId;
}