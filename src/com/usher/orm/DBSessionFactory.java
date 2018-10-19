package com.usher.orm;

import com.usher.bean.User;
import com.usher.utils.ORMAnnotationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
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

        /**
         * 查询所有数据
         * @param tClass
         * @param <T>
         * @return
         * @throws IllegalAccessException
         * @throws InstantiationException
         */
        public <T> List<T> list(Class<T> tClass) throws IllegalAccessException, InstantiationException, SQLException {
            //select * from tb
            String sql = "select %s from %s";
            //生成查询字段列表
            StringBuilder columns = new StringBuilder();
            Field[] fs = tClass.getDeclaredFields();
            for (int i = 0,len = fs.length; i < len; i++) {
                columns.append(ORMAnnotationUtil.getColumnName(fs[i]));
                if (i != len - 1) {
                    columns.append(",");
                }
            }

            //sql
            sql = String.format(sql, columns.toString(), ORMAnnotationUtil.getTableName(tClass));

            System.out.println("Statement SQL: " + sql);
            //execute sql(Statement,PrepareStatement)
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            List<T> list = new ArrayList<>();

            T obj = null;
            while (resultSet.next()) {
                //实例化实体类对象
                obj = tClass.newInstance();

                //读取指定字段的数据注入到实体类属性
                for (Field f : fs) {
                    //可访问性
                    f.setAccessible(true);
                    Class<?> type = f.getType();
                    if (type == String.class) {
                        f.set(obj, resultSet.getString(ORMAnnotationUtil.getColumnName(f)));
                    } else if (type == int.class || type == Integer.class) {
                        f.set(obj, resultSet.getInt(ORMAnnotationUtil.getColumnName(f)));
                    } else if (type == double.class || type == Double.class) {
                        f.set(obj, resultSet.getDouble(ORMAnnotationUtil.getColumnName(f)));
                    } else if (type == Date.class) {
                        f.set(obj, resultSet.getDate(ORMAnnotationUtil.getColumnName(f)));
                    }
                }

                //将实体类添加到list
                list.add(obj);
            }
            statement.close();
            return list;
        }

        public int save(Object object) {
            //插入一条数据
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
        System.out.println(userList);

    }

}
