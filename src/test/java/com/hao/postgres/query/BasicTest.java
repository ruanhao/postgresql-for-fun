package com.hao.postgres.query;

import com.hao.postgres.dto.IdAndName;
import com.hao.postgres.jpa.entity.Person;
import com.hao.postgres.jpa.repo.PersonRepository;
import com.hao.postgres.util.CommandRunner;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Sql("/sql/person.sql")
public class BasicTest {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    CommandRunner commandRunner;


    @PostConstruct
    public void setUp() {
        commandRunner.selectAll("person");
    }

    @Test
    public void findAll() {
        var all = personRepository.findAll();
        assertEquals(30, all.size());
        // with page
        var page = personRepository.findAll(PageRequest.of(1, 10));
        assertEquals(3, page.getTotalPages());
        assertEquals(30L, page.getTotalElements());
        var content = page.getContent();
        assertEquals(11, content.get(0).getId());
        assertEquals(20, content.get(content.size() - 1).getId());
    }

    @Test
    public void findBy() {
        assertEquals("Madonna", personRepository.findByName("Madonna").get(0).getName());
        // with page
        var firstPageOfMale = personRepository.findByGender("Male", PageRequest.of(0, 5));
        assertTrue(firstPageOfMale.getContent().size() > 0);
    }

    @Test
    public void count() {
        assertEquals(30, personRepository.count());
    }

    @Test
    public void getBy() { // lazy invocation
        assertEquals(1L, personRepository.getById(1L).getId());
        Person person100 = personRepository.getById(100L);
        assertNotNull(person100); // proxy instance

        try {
            assertEquals(100L, person100.getId());
            person100.getName();
            fail("Cannot be here");
        } catch (Exception e) {
            assertInstanceOf(LazyInitializationException.class, e);
        }

        assertNull(personRepository.getByName("Jack"),
                "not support getByXxx");

    }

    @Test
    public void findOneByExample() {
        assertTrue(personRepository.findOne(Example.of(new Person().setName("Madonna"),
                ExampleMatcher.matchingAny())).isPresent());
        assertFalse(personRepository.findOne(Example.of(new Person().setName("madonna"),
                ExampleMatcher.matchingAny())).isPresent());
        assertTrue(personRepository.findOne(Example.of(new Person().setName("madonna"),
                ExampleMatcher.matchingAny().withIgnoreCase())).isPresent());
        // by default, an EXACT match will be performed on ALL NON-NULL properties
        assertFalse(personRepository.findOne(Example.of(new Person().setName("Madonna").setAge(45))).isPresent());
        // ignoring properties
        assertTrue(personRepository.findOne(Example.of(new Person().setName("Madonna").setAge(45),
                ExampleMatcher.matchingAll()
                        .withIgnorePaths("id", "gender", "country", "company")))
                .isPresent());
        // property based matching
        assertTrue(personRepository.findOne(Example.of(new Person().setName("madonna").setAge(45),
                ExampleMatcher.matchingAll()
                        .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                        .withIgnorePaths("id")))  // or else it will match id column
                .isPresent());
    }

    @Test
    public void findOneBySpecification() {
        assertTrue(personRepository.findOne(((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(criteriaBuilder.lower(root.get("name")), "madonna"), // ignore case
                        criteriaBuilder.equal(root.get("age"), 45)
                ))).isPresent());

    }

    @Test
    public void projection() {
        assertTrue(personRepository.findById(1L, IdAndName.class).isPresent());
        assertEquals(30, personRepository.findAllBy(IdAndName.class).size());
    }

    @Test
    public void jpql() {
        List<Person> personWithIncInCompany = personRepository.search("inc", PageRequest.of(0, 10)).getContent();
        assertEquals(5, personWithIncInCompany.size());
        assertTrue(personWithIncInCompany.stream().allMatch(p -> p.getCompany().toLowerCase().contains("inc")));

        List<Person> personWithEliInName = personRepository.search("eli", PageRequest.of(0, 10)).getContent();
        assertEquals(2, personWithEliInName.size());
        assertTrue(personWithEliInName.stream().allMatch(p -> p.getName().toLowerCase().contains("eli")));
    }

}
