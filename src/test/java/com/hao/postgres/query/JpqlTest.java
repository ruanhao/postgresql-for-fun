package com.hao.postgres.query;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/person.sql")
public class JpqlTest {
}
