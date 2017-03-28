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

import io.joshworks.snappy.executor.AppExecutors;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Future;

/**
 * Created by Josh Gontijo on 3/25/17.
 */
public class AsyncJdbcRepository {

    private static AsyncQueryRunner asyncQueryRunner;

    static void init(DataSource ds) {
        QueryRunner queryRunner = new QueryRunner(ds);
        asyncQueryRunner = new AsyncQueryRunner(AppExecutors.executor(), queryRunner);
    }



    // BeanHandler<Exchange> exchangeBeanHandler = new BeanHandler<>(Exchange.class);
    // ResultSetHandler<List<Exchange>> h = new BeanListHandler<Exchange>(Exchange.class);
    public static Future<int[]> batch(String sql, Object[][] params) {
        try {
            return asyncQueryRunner.batch(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> query(String sql, ResultSetHandler<T> rsh, Object... params) {
        try {
            return asyncQueryRunner.query(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> query(String sql, ResultSetHandler<T> rsh) {
        try {
            return asyncQueryRunner.query(sql, rsh);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Future<Integer> update(String sql) {
        try {
            return asyncQueryRunner.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Future<Integer> update(String sql, Object param) {
        try {
            return asyncQueryRunner.update(sql, param);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Future<Integer> update(String sql, Object... params) {
        try {
            return asyncQueryRunner.update(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> insert(String sql, ResultSetHandler<T> rsh) {
        try {
            return asyncQueryRunner.insert(sql, rsh);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> insert(String sql, ResultSetHandler<T> rsh, Object... params) {
        try {
            return asyncQueryRunner.insert(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) {
        try {
            return asyncQueryRunner.insertBatch(sql, rsh, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource getDataSource() {
        return asyncQueryRunner.getDataSource();
    }

    public static boolean isPmdKnownBroken() {
        return asyncQueryRunner.isPmdKnownBroken();
    }

    public static void fillStatement(PreparedStatement stmt, Object... params) {
        try {
            asyncQueryRunner.fillStatement(stmt, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillStatementWithBean(PreparedStatement stmt, Object bean, PropertyDescriptor[] properties) {
        try {
            asyncQueryRunner.fillStatementWithBean(stmt, bean, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillStatementWithBean(PreparedStatement stmt, Object bean, String... propertyNames) {
        try {
            asyncQueryRunner.fillStatementWithBean(stmt, bean, propertyNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
