package com.hao.postgres.dto;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@ToString
public class RequestContext {

    @Getter
    private final String tenantId;

    @Getter
    private final String correlationId;

    @Getter
    private final String username;

    public static final String SYSTEM_USER_USERNAME = "system";

    private RequestContext(final String tenantId, final String correlationId, final String username) {
        this.tenantId = tenantId;
        this.correlationId = correlationId;
        this.username = username;
    }
    private RequestContext(final String tenantId) {
        this(tenantId, UUID.randomUUID().toString(), SYSTEM_USER_USERNAME);
    }

    public static RequestContext of(final String tenantId) {
        return new RequestContext(Objects.requireNonNull(tenantId));
    }
}
