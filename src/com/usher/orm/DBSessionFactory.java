package com.usher.orm;

import com.usher.bean.User;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author: Usher
 * @Description:
 * 装饰者模式，工厂模式
 */
public class DBSessionFactory {
    //数据源
    private DBSource dbSource;
    //数据源连接属性
    private Properties properties;
    public DBSessionFactory() throws Exception {
        properties = new Properties();
        //从属性资源 加载key-value
        properties.load(ClassLoader.getSystemResourceAsStream("resources/dbConfig.properties"));
        //System.out.println(properties.getProperty("url"));
        dbSource = new DBSource(properties);
        //Connection connection = dbSource.openConnection();
        //System.out.println("连接成功");
    }

    //打开一个数据库连接
    public DBSession openSession() throws Exception {
        return new DBSession(dbSource.openConnection());
    }
    /**
     * 操作数据库
     */
    public static class DBSession {
        private Connection connection;//数据库连接对象

        public DBSession(Connection connection) {
            this.connection = connection;
        }

        public <T> List<T> list(Class<T> tClass) throws IllegalAccessException, InstantiationException {
            List<T> list = new ArrayList<>();
            list.add(tClass.newInstance());
            list.add(tClass.newInstance());
            return list;
        }
        /**
         * 关闭连接
         */
        public void close() {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }finally {
                    connection = null;
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        //Test
        DBSessionFactory sessionFactory = new DBSessionFactory();
        List<User> userList = sessionFactory.openSession().list(User.class);
        System.out.println(userList.size());

    }

}
