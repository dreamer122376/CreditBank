package com.creditbank.mvp.config;

import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource dataSource;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DatabaseInitializer(DataSource dataSource, SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化数据库...");
        try (Connection connection = dataSource.getConnection()) {
            executeSqlFile(connection, "/db/credit_bank.sql");
            executeSqlFile(connection, "/db/data.sql");
            ensureAdminUserExists();
            logger.info("数据库初始化完成");
        } catch (SQLException e) {
            logger.error("数据库初始化失败", e);
            throw e;
        }
    }

    private void ensureAdminUserExists() {
        SysUser existing = sysUserMapper.selectById(1L);
        if (existing == null) {
            logger.info("创建默认管理员账户...");
            SysUser admin = new SysUser();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setPhone("13800138000");
            admin.setEmail("admin@creditbank.com");
            admin.setRole("admin");
            admin.setBalance(0);
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            sysUserMapper.insert(admin);
            logger.info("默认管理员账户创建完成");
        } else {
            if (!passwordEncoder.matches("123456", existing.getPassword())) {
                logger.info("更新管理员密码为BCrypt加密...");
                sysUserMapper.update(
                        null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUser>()
                                .eq(SysUser::getId, existing.getId())
                                .set(SysUser::getPassword, passwordEncoder.encode("123456"))
                );
            }
        }
        ensureAllUserPasswordsEncrypted();
    }

    private void ensureAllUserPasswordsEncrypted() {
        java.util.List<SysUser> users = sysUserMapper.selectList(null);
        int updatedCount = 0;
        for (SysUser user : users) {
            if (!passwordEncoder.matches("123456", user.getPassword())) {
                sysUserMapper.update(
                        null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUser>()
                                .eq(SysUser::getId, user.getId())
                                .set(SysUser::getPassword, passwordEncoder.encode("123456"))
                );
                updatedCount++;
            }
        }
        if (updatedCount > 0) {
            logger.info("已更新 {} 个用户的密码为BCrypt加密", updatedCount);
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
//        String upperSql = sql.toUpperCase();
//        if (upperSql.contains("DROP DATABASE") || upperSql.contains("DROP SCHEMA") || upperSql.contains("DROP TABLE")) {
//            logger.warn("跳过危险SQL，避免启动时删除已有数据: {}", sql.substring(0, Math.min(100, sql.length())));
//            return;
//        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.warn("执行SQL失败，可能已存在: {}", sql.substring(0, Math.min(100, sql.length())));
        }
    }
}