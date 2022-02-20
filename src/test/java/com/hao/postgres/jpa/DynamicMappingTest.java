package com.hao.postgres.jpa;

import com.hao.postgres.jpa.entity.Person;
import com.hao.postgres.jpa.repo.MalePersonRepository;
import com.hao.postgres.jpa.repo.PersonRepository;
import com.hao.postgres.util.TransactionalExecutor;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/person.sql")
public class DynamicMappingTest {

    @Autowired
    MalePersonRepository malePersonRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    TransactionalExecutor transactionalExecutor;

    @PersistenceContext
    public EntityManager entityManager;

    @Test
    public void where() {
        Assertions.assertFalse(malePersonRepository.findAll().isEmpty());
        Assertions.assertTrue(malePersonRepository.findAll().stream().allMatch(p -> Objects.equals("Male", p.getGender())));
    }

    Session session() {
        return entityManager.unwrap(Session.class);
    }

    // The @Filter annotation works the same way as @Where,
    // but it also can be enabled or disabled on SESSION LEVEL (require transaction), and also parameterized.
    @Test
    public void filter() {
        transactionalExecutor.required(() ->  {
            session().enableFilter(Person.AGE_FILTER_NAME).setParameter(Person.AGE_FILTER_ARGUMENT_NAME, 40);
            Assertions.assertTrue(personRepository.findAll().stream().allMatch(p -> p.getAge() > 40));
            session().disableFilter(Person.AGE_FILTER_NAME);
        });

        transactionalExecutor.required(() ->  {
            session().enableFilter(Person.AGE_FILTER_NAME).setParameter(Person.AGE_FILTER_ARGUMENT_NAME, 40);
            Assertions.assertFalse(malePersonRepository.findAll().stream().allMatch(p -> p.getAge() > 40),
                    "Because @Filter is not attached on MalePerson"
                    );
            session().disableFilter(Person.AGE_FILTER_NAME);
        });

    }
}
