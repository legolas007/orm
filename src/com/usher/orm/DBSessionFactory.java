package com.usher.orm;

import com.usher.bean.User;
import com.usher.utils.ORMAnnotationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

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

        public int save(Object object) throws SQLException, IllegalAccessException {
            //插入一条数据
            //生成sql：insert into tb() values()
            String sql = "insert into %s(%s) values(%s)";
            StringBuilder columns = new StringBuilder();
            StringBuilder params = new StringBuilder();

            //获取实体对象的所有字段
            Field[] fs = object.getClass().getDeclaredFields();
            for (int i = 0, len = fs.length; i < len; i++) {
                columns.append(ORMAnnotationUtil.getColumnName(fs[i]));
                params.append("?");

                if (i != len - 1) {
                    columns.append(",");
                    params.append(",");
                }
            }

            //生成sql
            sql = String.format(sql, ORMAnnotationUtil.getTableName(object.getClass()),
                    columns.toString(), params.toString());
            System.out.println("Insert SQL: " + sql);

            //创建预处理SQL对象
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            //设置预处理的参数
            int i = 1;//sql从1开始
            for (Field f : fs) {
                //可访问性
                f.setAccessible(true);
                Class<?> type = f.getType();
                if (type == String.class) {
                    preparedStatement.setString(i, String.valueOf(f.get(object)));
                } else if (type == int.class || type == Integer.class) {
                    preparedStatement.setInt(i, f.getInt(object));
                } else if (type == double.class || type ==Double.class) {
                    preparedStatement.setDouble(i, f.getDouble(object));
                }
                i++;
            }
            //执行预处理语句
            int rows = preparedStatement.executeUpdate();
            preparedStatement.close();
            return rows;
        }

        public int update(Object object) throws IllegalAccessException, SQLException {
            String sql = "update %s set %s where %s";
            StringBuilder updateColumns = new StringBuilder();
            String where = "";

            Field[] fs = object.getClass().getDeclaredFields();
            //更新字段集合
            List<Field> updateFields = new ArrayList<>();
            Field f = null;
            for (int i = 0, len = fs.length; i < len; i++) {
                f = fs[i];
                //判断字段是否为主键
                if (ORMAnnotationUtil.isId(f)) {
                    f.setAccessible(true);
                    where = ORMAnnotationUtil.getColumnName(f) + "=";
                    //判断主键字段类型
                    if (f.getType() == String.class) {
                        where += "'" + String.valueOf(f.get(object)) + "'";
                    } else {
                        where += f.get(object);
                    }
                    continue;
                }
                //非主键
                updateColumns.append(ORMAnnotationUtil.getColumnName(f) + "=?");
                if (i != len - 1) {
                    updateColumns.append(",");
                }
                //将更新的字段添加到集合
                updateFields.add(f);
                f = null;
            }
            sql = String.format(sql,
                    ORMAnnotationUtil.getTableName(object.getClass()),
                    updateColumns.toString(), where);

            System.out.println("Update SQL:" + sql);

            //执行
            PreparedStatement preparedStatement =connection.prepareStatement(sql);
            Class<?> type = null;
            for (int i = 0, len = updateFields.size(); i < len; i++) {
                f = updateFields.get(i);
                f.setAccessible(true);

                type = f.getType();//字段类型
                if (type == String.class) {
                    preparedStatement.setString(i + 1, String.valueOf(f.get(object)));
                } else if (type == int.class || type == Integer.class) {
                    preparedStatement.setInt(i + 1, f.getInt(object));
                } else if (type == double.class || type == Double.class) {
                    preparedStatement.setDouble(i + 1, f.getDouble(object));
                } else if (type == long.class || type == Long.class) {
                    preparedStatement.setLong(i + 1, f.getLong(object));
                } else if (type == float.class || type == Float.class) {
                    preparedStatement.setFloat(i + 1, f.getFloat(object));
                } else if (type == Date.class) {
                    Date date = (Date) f.get(object);
                    preparedStatement.setDate(i + 1, new java.sql.Date(date.getTime()));
                }
            }

            int rows = preparedStatement.executeUpdate();
            preparedStatement.close();

            return rows;
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
        DBSession session = sessionFactory.openSession();
        List<User> userList = sessionFactory.openSession().list(User.class);
        System.out.println(userList);
        User user = new User();
       // user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        user.setId("1");
        user.setUsername("usher");
        user.setPassword("1234567");
        user.setNickname("usher");
        user.setPhone("323232");

        System.out.println(session.update(user));

    }

}
