package com.usher.utils;

import com.usher.annotation.Column;
import com.usher.annotation.Table;

import java.lang.reflect.Field;

/**
 * @Author: Usher
 * @Description:
 * 实现orm框架解析注解
 */
public class ORMAnnotationUtil {
    /**
     *指定类上的注入的表名
     * @return
     */
    public static String getTableName(Class<?> beanClass) {
        //通过反射获取@Table注解
        Table table = beanClass.getAnnotation(Table.class);
        if (table == null) {
            return beanClass.getSimpleName().toLowerCase();
        }

        return table.value();
    }

    /**
     * 返回指定字段列名
     * @return
     */
    public static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            //获取字段名称
            return field.getName().toLowerCase();
        }
        return column.value();
    }

    /**
     * 从类中查询主键的列
     * @param cls
     * @return
     */
    public static Field findIdField(Class<?> cls) {
        for (Field f : cls.getDeclaredFields()) {
            if (isId(f)) {
                return f;
            }
        }
        return null;
    }
    public static boolean isId(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            //获取字段主键
            return column.isId();
        }
        return false;
    }
}
