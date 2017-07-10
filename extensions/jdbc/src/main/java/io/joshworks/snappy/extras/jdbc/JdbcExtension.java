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
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.property.AppProperties;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Josue on 07/02/2017.
 */
public class JdbcExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(JdbcExtension.class);

    private static final String EXTENSION_NAME = "JDBC";
    private static final String JDBC_PREFIX = "jdbc";

    private static final String SCRIPT_PREFIX = JDBC_PREFIX + ".script.";
    private static final String SQL_SCRIPT_FILE = SCRIPT_PREFIX + "file";
    private static final String AUTOCOMMIT = SCRIPT_PREFIX + "autocommit";
    private static final String FAIL_ON_ERROR = SCRIPT_PREFIX + "failonerror";

    //Hikary properties
    private static final String HIKARI_PREFIX = JDBC_PREFIX + ".hikari.";
    private static final String DS_CLASS_NAME = HIKARI_PREFIX + "dataSourceClassName";
    private static final String DS_DRIVER = HIKARI_PREFIX + "dataSource.driver";
    private static final String DS_URL = HIKARI_PREFIX + "dataSource.url";


    private HikariDataSource dataSource;

    @Override
    public void onStart(ServerData config) {
        if (dataSource != null) {
            throw new IllegalStateException("Datasource already configured");
        }
        Properties properties = AppProperties.getProperties();

        logger.info("Initializing Datasource connection pool");

        Properties hikariProps = new Properties();
        properties.forEach((key, val) -> {
            String hikaryKey = String.valueOf(key);
            if (hikaryKey.startsWith(HIKARI_PREFIX)) {
                hikariProps.put(hikaryKey.replace(HIKARI_PREFIX, ""), val);
            }
        });

        this.dataSource = new HikariDataSource(new HikariConfig(hikariProps));
        JdbcRepository.init(dataSource);
        AsyncJdbcRepository.init(dataSource);

        //dbUtils
        DbUtils.loadDriver(properties.getProperty(DS_DRIVER));

        String scriptFile = properties.getProperty(SQL_SCRIPT_FILE);
        if (scriptFile != null && !scriptFile.isEmpty()) {
            logger.info("Running SQL start script '" + scriptFile + "'");
            runStartScript(scriptFile, properties);
            logger.info("Script execution of '" + scriptFile + "' completed");
        } else {
            logger.info("No SQL start script not found");
        }
    }

    @Override
    public void onShutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Shutting down datasource");
            dataSource.close();
        }
    }

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

    private void runStartScript(String scriptFile, Properties properties) {
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
}
