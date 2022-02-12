package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.City;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByName(String name);
    Page<City> findAll(Pageable pageable);
}
