package com.hao.postgres.jpa.repo;

import com.hao.postgres.jpa.entity.Network;
import java.util.List;

public interface NetworkRepository extends BaseRepository<Network, Long> {
    List<Network> findByName(String name);
}
