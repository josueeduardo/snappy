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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by Josue on 07/02/2017.
 */
public class DatabaseConfiguration {

    private static final String SQL_SCRIPT_FILE = "script.file";
    private static final String AUTOCOMMIT = "script.autocommit";
    private static final String FAIL_ON_ERROR = "script.failonerror";

    //Hikary properties
    private static final String HIKARI_PREFIX = "hikari.";
    private static final String DS_CLASS_NAME = HIKARI_PREFIX + "dataSourceClassName";
    private static final String DS_DRIVER = HIKARI_PREFIX + "dataSource.driver";
    private static final String DS_URL = HIKARI_PREFIX + "dataSource.url";
    private static final Logger logger = Logger.getLogger(DatabaseConfiguration.class.getName());


    private Properties properties;
    private HikariDataSource dataSource;

    public void configure() {
        logger.info(":: Initializing Datasource connection pool ::");


        Properties hikariProps = new Properties();
        properties.forEach((key, val) -> {
            String hikaryKey = String.valueOf(key);
            if (hikaryKey.startsWith(HIKARI_PREFIX)) {
                hikariProps.put(hikaryKey.replace(HIKARI_PREFIX, ""), val);
            }
        });

        dataSource = new HikariDataSource(new HikariConfig(hikariProps));

        //dbUtils
        DbUtils.loadDriver(properties.getProperty(DS_DRIVER));

        String scriptFile = properties.getProperty(SQL_SCRIPT_FILE);
        if (scriptFile != null && !scriptFile.isEmpty()) {
            logger.info("Running SQL start script '" + scriptFile + "' ::");
            runStartScript(scriptFile);
        } else {
            logger.info("No SQL start script not found");
        }
    }

    private void runStartScript(String scriptFile) {
        String autoCommit = properties.getProperty(AUTOCOMMIT, "false");
        String failOnError = properties.getProperty(FAIL_ON_ERROR, "true");

        Connection conn = null;
        try {
            InputStream sqlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptFile);
            if (sqlFile == null) {
                throw new RuntimeException("SQL script not found: " + scriptFile);
            }

            conn = dataSource.getConnection();
            ScriptRunner runner = new ScriptRunner(conn, Boolean.parseBoolean(autoCommit), Boolean.parseBoolean(failOnError));
            runner.runScript(new InputStreamReader(sqlFile));

        } catch (SQLException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    throw new RuntimeException("Error rolling back script execution", e1);
                }
            e.printStackTrace();

        } catch (IOException e) {
            throw new RuntimeException("Could not find sql file '" + scriptFile + "'", e);
        }
    }

    public DataSource produceDatasource() {
        return dataSource;
    }

    public QueryRunner prodyceQueryRunner() {
        return new QueryRunner(dataSource);
    }

}
