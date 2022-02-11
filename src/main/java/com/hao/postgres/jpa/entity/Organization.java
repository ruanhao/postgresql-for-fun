package com.hao.postgres.jpa.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity // @Entity annotation specifies that the class is an entity and is mapped to a database table.
@Table(
        name = "organizations",  // specifies the name of the database table to be used for mapping.
        indexes = @Index(name = "organizations_ukey_tenant_id_name", columnList = "tenantId,name", unique = true)
)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @NotNull
    String tenantId;

    @Size(max = 64)
    @NotNull
    String name;

    @Column(columnDefinition = "integer default 86")
    int countryCode;

    String description;

    boolean active;
}
