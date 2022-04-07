package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.Person;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends BaseRepository<Person, Long> {

    List<Person> findByName(String name);

    Page<Person> findAll(Pageable pageable);

    Page<Person> findByGender(String gender, Pageable pageable);

    Person getByName(String name);

    @Query(
            value = "select * from person where lower(name) like concat('%', lower(:search), '%') or lower(company) like concat('%', lower(:search), '%')",
            nativeQuery = true)  // By default, the query definition uses JPQL.
                                 // 'nativeQuery = true' indicates to use native SQL (postgres)

    Page<Person> search(String search, Pageable pageable);

}
