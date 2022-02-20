package com.hao.postgres.sql;

import com.hao.postgres.util.CommandRunner;
import java.util.Objects;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/sql-practice-1.sql")
public class QueryTest {

    @Autowired
    CommandRunner commandRunner;

    /**
     * average salary of department > 2000
     */
    void having() {
        String sql = "select deptno, round(avg(sal)) from emp group by deptno having avg(sal) >  2000;";
        Assertions.assertTrue(commandRunner.psql(sql).contains("2 rows"));

        // Postgres do not support alias when using having, instead:
        sql = String.join("\n",
                "select * from (" +
                        "select deptno, round(avg(sal)) as avgsal from emp group by deptno having avg(sal) >  2000" +
                        ") as ss " + " where avgsal > 2500;" // postgres sucks where you have to specify an alias which is not necessary
        );
        Assertions.assertTrue(commandRunner.psql(sql).contains("1 row"));

    }

    @Test
    public void group() {
        having();
        aggregationFunctionInHaving(); // having 后可以使用聚合函数
        innerJoinThenGrouping();
        groupingThenLimit();
        usingFunctionWhenAggregate(); // 对函数结果进行聚合操作
    }

    /**
     * 按部门计算服务年限平均值
     */
    void usingFunctionWhenAggregate() {
        var sql = "select e.deptno, round(avg(extract(year from age(now(), e.hiredate)))) from emp e group by e.deptno;";
        var output = commandRunner.psql(sql);
        assert output.contains("10 |    40");
        assert output.contains("20 |    38");
        assert output.contains("30 |    40");
    }

    /**
     * 至少有 5 个员工的所有部门
     */
    void aggregationFunctionInHaving() {
        var sql = "select e.deptno from emp e group by e.deptno having count(*) >= 5;";
        assert commandRunner.psql(sql).contains("2 rows");
    }

    /**
     * 平均薪水最高的部门编号
     */
    void groupingThenLimit() {
        var sql = "select deptno, avg(sal) from emp group by deptno order by avg desc limit 1;";
        commandRunner.psql(sql).contains("10 | 2916.6");
    }

    /**
     * 部门中所有人平均的薪水等级
     */
    void innerJoinThenGrouping() {
        val sql = "\n" +
                "select \n" +
                "    e.deptno, round(avg(s.grade), 3)\n" +
                "from \n" +
                "    emp e, salgrade s \n" +
                "where \n" +
                "    e.sal between s.losal and s.hisal\n" +
                "group by \n" +
                "    e.deptno;" +
                "\n";
        assert commandRunner.psql(sql).contains("10 | 3.667");
    }

    void distinctCount() {
        val sql = "select count(distinct(job)) from emp;"; // 整体作为一个 group
        val output = commandRunner.psql(sql);
        assert output.contains("5");
        assert output.contains("1 row");
    }

    @Test
    public void distinct() {
        // distinct takes effect for all fields
        assert commandRunner.psql("select distinct deptno, job from emp;").contains("9 rows");

        distinctCount();
    }

    void selfJoin() {
        var sqlV1 = "select a.ename, b.ename from emp a, emp b where a.mgr = b.empno;";
        var sqlV2 = "select a.ename, b.ename from emp as a join emp as b on a.mgr = b.empno;";
        var outputV1 = commandRunner.psql(sqlV1);
        var outputV2 = commandRunner.psql(sqlV2);
        assert Objects.equals(outputV1, outputV2);
        assert outputV1.contains("JAMES  | BLAKE");
        assert !outputV1.contains("KING  |");  // KING has no manager, see self outer join
    }

    void selfOuterJoin() {
        var sql = "select a.ename, b.ename from emp as a left join emp b on a.mgr = b.empno;";
        var output = commandRunner.psql(sql);
        assert output.contains("14 rows");
        assert output.contains("KING   |");
    }


    /**
     *   <pre>
     *        from
     *          A
     *        join // A 和 B 先连接
     *          B
     *        on
     *          ..
     *        join // 上面的结果再与 C 连接
     *          C
     *        on
     *          ..
     *    </pre>
     */
    void multiJoin() {
        // 查询员工的部门名和工资等级
        var sql = "\n" +
                "select\n" +
                "            e.ENAME, d.DNAME, s.GRADE\n" +
                "        from\n" +
                "            EMP as e\n" +
                "        left join\n" +
                "            DEPT as d\n" +
                "        ON\n" +
                "            e.DEPTNO = d.DEPTNO\n" +
                "        left join\n" +
                "            SALGRADE as s\n" +
                "        ON\n" +
                "            e.SAL between s.LOSAL and s.HISAL\n";
        assert commandRunner.psql(sql).contains("14 rows");

    }

    /**
     * - 内连接：A 表 和 B 表无主副之分，谁前谁后无区别 <br/>
     * - 外连接：A 表 和 B 表有主副之分，主要查询主表
     */
    @Test
    public void join() {
        selfJoin(); // self and inner
        selfOuterJoin();
        multiJoin();
        selfLeftJoin(); // self comparison
        selfJoinAndGroup();

    }

    /**
     * 列出所有职务为 CLERK 的员工姓名和所属部门人数
     */
    void selfJoinAndGroup() {
        val sql = "\n" +
                "select\n" +
                "    e1.ename, e1.deptno, count(*) \n" +
                "from \n" +
                "    emp e1 \n" +
                "join \n" +
                "    emp e2 \n" +
                "on \n" +
                "    e1.deptno = e2.deptno and e1.job = 'CLERK' \n" +
                "group by \n" +
                "    e1.ename, e1.deptno;\n";
        assert commandRunner.psql(sql).contains("4 rows");
    }


    /**
     * 使用自连接代替 max 函数
     */
    void selfLeftJoin() {
        var sql = "select e1.sal from emp e1 left join emp e2 on e1.sal < e2.sal where e2.sal is null;";
        commandRunner.psql(sql).contains("5000");
    }

    /**
     * 每个部门平均薪水工资等级
     */
    void subSelect1() {
        val sql = "\n" +
                "select \n" +
                " ss.dname, s.grade\n" +
                "from\n" +
                " (\n" +
                "   select \n" +
                "    d.dname, avg(sal) \n" +
                " from emp as e \n" +
                " left join dept as d \n" +
                "    on e.deptno = d.deptno\n" +
                " group by d.dname\n" +
                " ) as ss\n" +
                "left join \n" +
                " salgrade as s \n" +
                "on \n" +
                " ss.avg between s.losal and s.hisal;\n";
        val output = commandRunner.psql(sql);
        assert output.contains("3 rows");
        assert output.contains("ACCOUNTING |     4");
        assert output.contains("SALES      |     3");
        assert output.contains("RESEARCH   |     4");
    }

    /**
     * 高于平均薪资的员工
     */
    void subSelect2() {
        val sql = "select * from emp e where e.sal > (select avg(sal) from emp);";
        assert commandRunner.psql(sql).contains("6 rows");
    }


    /**
     * 将查询结果排列在一起
     */
    void subSelect3() {
        val sql = "select (select count(*) from emp), (select round(avg(sal)) from emp);";
        assert commandRunner.psql(sql).contains("1 row");
    }

    /**
     * 部门最高薪水的员工姓名
     */
    void subSelect4() {
        var sql = "\n" +
                "select \n" +
                "    e.ename from emp e \n" +
                "where\n" +
                "    e.sal in (select max(sal) from emp e group by e.deptno);\n";
        String output = commandRunner.psql(sql);
        assert output.contains("4 rows");
        assert output.contains("BLAKE");
        assert output.contains("SCOTT");
        assert output.contains("KING");
        assert output.contains("FORD");

    }

    /**
     *  select 语句中嵌套 select 语句，被嵌套的 select 语句就是子查询。 <br/>
     *  可以出现的位置: <br/>
     *  - select 后 <br/>
     *  - from  后 (将子查询的结果当做一张临时表) <br/>
     *  - where 后
     */
    @Test
    public void subSelect() {
        subSelect1(); // after from
        subSelect2(); // after where
        subSelect3(); // after select
        subSelect4(); // after where
        subSelect5(); // after from
        subSelect6(); // multiple sub select (after where)
    }

    /**
     * 比普通员工最高工资还高的领导
     */
    void subSelect6() {
        val sql = "\n" +
                "select \n" +
                "    * \n" +
                "from \n" +
                "    emp \n" +
                "where \n" +
                "    sal > (\n" +
                "              select \n" +
                "                  max(sal) \n" +
                "              from \n" +
                "                  emp \n" +
                "              where \n" +
                "                  empno not in (\n" +
                "                                   select \n" +
                "                                       distinct(mgr) \n" +
                "                                   from \n" +
                "                                       emp \n" +
                "                                   where \n" +
                "                                       mgr is not null\n" +
                "                                )\n" +
                "            );";
        assert commandRunner.psql(sql).contains("6 rows");
    }

    /**
     * 薪水在部门平均工资之上的人员名称
     */
    void subSelect5() {
        val sql = "\n" +
                "select \n" +
                "    e.ename \n" +
                "from \n" +
                "    emp e, (select deptno, avg(sal) from emp group by deptno) t \n" +
                "where \n" +
                "    e.deptno = t.deptno and e.sal > t.avg;";
        assert commandRunner.psql(sql).contains("6 rows");
    }
}
