package com.hao.postgres.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Filter(
        name = Person.AGE_FILTER_NAME,
        condition = Person.AGE_PROPERTY_NAME + "> :" + Person.AGE_FILTER_ARGUMENT_NAME
)
@Entity
@Table(name = "person")
public class Person {

    public static final String AGE_FILTER_NAME = "personAgeFilter";
    public static final String AGE_FILTER_ARGUMENT_NAME = "ageLimit";

    public static final String AGE_PROPERTY_NAME = "age";

    @Id
    long id;

    String name;

    int age;

    String gender;

    String country;

    String company;

}
