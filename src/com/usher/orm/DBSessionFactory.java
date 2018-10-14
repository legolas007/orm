package com.usher.orm;

import java.util.Properties;

/**
 * @Author: Usher
 * @Description:
 */
public class DBSessionFactory {
    //数据源
    private DBSource dbSource;
    //数据源连接属性
    private Properties properties;
    public DBSessionFactory(DBSource dbSource) {
        this.dbSource = dbSource;
    }

}
