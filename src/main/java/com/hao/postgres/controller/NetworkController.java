package com.hao.postgres.controller;

import com.hao.postgres.jpa.entity.Network;
import com.hao.postgres.service.NetworkService;
import com.hao.postgres.util.DbUtils;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class NetworkController {

    @Autowired
    NetworkService networkService;

    @Autowired
    TransactionManager tm;

    @Autowired
    DataSource ds;

    @Value("classpath:sql/networks.sql")
    Resource scriptResource;

    @GetMapping("/restore")
    @SneakyThrows
    public void restoreTable() {
        DbUtils.executeSqlScript(ds.getConnection(), scriptResource);
    }

    @GetMapping("/no-tx")
    public List<Network> noTx() {
        return networkService.findByName("Site-Cisco");
    }

    @GetMapping("/tx")
    public List<Network> tx() {
        return networkService.findByNameInTx("Site-Cisco");
    }


}
