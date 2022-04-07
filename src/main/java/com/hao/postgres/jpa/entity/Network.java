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
        name = "networks",  // specifies the name of the database table to be used for mapping.
        indexes = @Index(name = "networks_ukey_tenant_id_name", columnList = "tenantId,name", unique = true)
)
public class Network extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;



    @Size(max = 64)
    @NotNull
    String name;


}
