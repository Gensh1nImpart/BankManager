package com.bankmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 应用配置。优先级：环境变量 &gt; config.properties &gt; 内置默认值。
 *
 * <p>支持的环境变量：
 * <ul>
 *     <li>{@code BANK_DB_URL}、{@code BANK_DB_USER}、{@code BANK_DB_PASSWORD}</li>
 *     <li>{@code BANK_POOL_SIZE}</li>
 * </ul>
 */
public final class Config {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/bank_manager"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";

    private final Properties props = new Properties();

    private Config() {
    }

    public static Config load() {
        Config config = new Config();
        Path file = Path.of("config.properties");
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                config.props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("读取 config.properties 失败", e);
            }
        }
        return config;
    }

    public String dbUrl() {
        return value("db.url", "BANK_DB_URL", DEFAULT_URL);
    }

    public String dbUser() {
        return value("db.user", "BANK_DB_USER", "root");
    }

    public String dbPassword() {
        return value("db.password", "BANK_DB_PASSWORD", "");
    }

    public int poolSize() {
        return Integer.parseInt(value("db.poolSize", "BANK_POOL_SIZE", "5"));
    }

    private String value(String key, String env, String fallback) {
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return props.getProperty(key, fallback).trim();
    }
}
