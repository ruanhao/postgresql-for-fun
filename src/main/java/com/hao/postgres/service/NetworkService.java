package com.hao.postgres.service;

import com.hao.postgres.jpa.entity.Network;
import com.hao.postgres.jpa.repo.NetworkRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NetworkService {

    @Autowired
    NetworkRepository networkRepository;

    public List<Network> findByName(String name) {
        List<Network> networks = networkRepository.findByName(name);
        return networks;
    }

    @Transactional
    public List<Network> findByNameInTx(String name) {
        List<Network> networks = networkRepository.findByName(name);
        return networks;
    }
}
