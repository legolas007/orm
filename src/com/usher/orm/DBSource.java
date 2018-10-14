package com.usher.orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @Author: Usher
 * @Description:
 */
public class DBSource {
    private String driver;
    private String url;
    private String username;
    private String password;

    public DBSource() {
    }

    public DBSource(Properties properties) {
        this.driver = properties.getProperty("driver");
        this.url = properties.getProperty("url");
        this.username = properties.getProperty("username");
        this.password = properties.getProperty("password");
    }

    /**
     * 数据源工具类
     * @return
     * @throws Exception
     */
    public Connection openConnection() throws Exception{
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
