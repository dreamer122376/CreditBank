package com.creditbank.mvp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource dataSource;

    @Autowired
    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化数据库...");
        try (Connection connection = dataSource.getConnection()) {
            executeSqlFile(connection, "/db/credit_bank.sql");
            executeSqlFile(connection, "/db/data.sql");
            logger.info("数据库初始化完成");
        } catch (SQLException e) {
            logger.error("数据库初始化失败", e);
            throw e;
        }
    }

    private void executeSqlFile(Connection connection, String filePath) throws SQLException, IOException {
        logger.info("执行SQL文件: {}", filePath);
        InputStream inputStream = getClass().getResourceAsStream(filePath);
        if (inputStream == null) {
            logger.warn("SQL文件不存在: {}", filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    continue;
                }
                sqlBuilder.append(line);
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    if (!sql.trim().isEmpty()) {
                        executeSql(connection, sql);
                    }
                    sqlBuilder = new StringBuilder();
                }
            }
        }
    }

    private void executeSql(Connection connection, String sql) throws SQLException {
        if (sql.trim().isEmpty()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.warn("执行SQL失败，可能已存在: {}", sql.substring(0, Math.min(100, sql.length())));
        }
    }
}