package com.hao.postgres.jpa.entity;

import javax.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cities", indexes = @Index(name = "cities_ukey_name", columnList = "name", unique = true))
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String name;

    int population;
}
