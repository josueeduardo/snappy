/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.extras.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Josh Gontijo on 3/25/17.
 */
public class JdbcRepository {

    private static QueryRunner queryRunner;

    static void init(DataSource ds) {
        queryRunner = new QueryRunner(ds);
    }


    // BeanHandler<Exchange> exchangeBeanHandler = new BeanHandler<>(Exchange.class);
    // ResultSetHandler<List<Exchange>> h = new BeanListHandler<Exchange>(Exchange.class);
    public static int[] batch(String sql, Object[][] params) {
        try {
            return queryRunner.batch(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) {
        try {
            return queryRunner.query(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T query(String sql, ResultSetHandler<T> rsh) {
        try {
            return queryRunner.query(sql, rsh);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(String sql) {
        try {
            return queryRunner.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(String sql, Object param) {
        try {
            return queryRunner.update(sql, param);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(String sql, Object... params) {
        try {
            return queryRunner.update(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T insert(String sql, ResultSetHandler<T> rsh) {
        try {
            return queryRunner.insert(sql, rsh);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) {
        try {
            return queryRunner.insert(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) {
        try {
            return queryRunner.insertBatch(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource getDataSource() {
        return queryRunner.getDataSource();
    }

    public static boolean isPmdKnownBroken() {
        return queryRunner.isPmdKnownBroken();
    }

    public static void fillStatement(PreparedStatement stmt, Object... params) {
        try {
            queryRunner.fillStatement(stmt, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillStatementWithBean(PreparedStatement stmt, Object bean, PropertyDescriptor[] properties) {
        try {
            queryRunner.fillStatementWithBean(stmt, bean, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillStatementWithBean(PreparedStatement stmt, Object bean, String... propertyNames) {
        try {
            queryRunner.fillStatementWithBean(stmt, bean, propertyNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
