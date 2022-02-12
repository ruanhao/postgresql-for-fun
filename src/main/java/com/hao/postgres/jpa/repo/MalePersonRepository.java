package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.MalePerson;
import org.springframework.stereotype.Repository;

@Repository
public interface MalePersonRepository extends BaseRepository<MalePerson, Long> {

}
