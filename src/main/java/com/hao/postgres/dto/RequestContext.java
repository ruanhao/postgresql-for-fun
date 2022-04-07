package com.hao.postgres.dto;

import java.util.Objects;
import lombok.Getter;
import lombok.ToString;

@ToString
public class RequestContext {

    @Getter
    private final String tenantId;

    private RequestContext(final String tenantId) {
        this.tenantId = tenantId;
    }

    public static RequestContext of(final String tenantId) {
        return new RequestContext(Objects.requireNonNull(tenantId));
    }
}
