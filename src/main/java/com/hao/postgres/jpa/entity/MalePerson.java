package com.hao.postgres.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "person")
@Where(clause = "gender = 'Male'")
public class MalePerson {

    @Id
    long id;

    String name;

    int age;

    String gender;

    String country;

    String company;

}
