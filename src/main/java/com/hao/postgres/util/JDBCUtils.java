package com.hao.postgres.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JDBCUtils {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    public <S> S findOne(String sql, TypeReference<S> resultType) {
//        return objectMapper.convertValue(jdbcTemplate.queryForMap(sql), resultType);
//    }

    public <S> S findOne(String sql, Class<S> resultType) {
        return objectMapper.convertValue(jdbcTemplate.queryForMap(sql), resultType);
    }

    @SneakyThrows
    public <S> List<S> findMulti(String sql, Class<S> resultType) {
        List<S> result = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : jdbcTemplate.queryForList(sql)) {
            result.add(objectMapper.convertValue(stringObjectMap, resultType));
        }
        return result;
    }

    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }



}
