package com.hao.postgres.util;

import java.sql.Connection;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;


public class DbUtils {


    @SneakyThrows
    public static void executeSqlScript(Connection connection, Resource resource) {
        ScriptUtils.executeSqlScript(connection, resource);
    }


}
