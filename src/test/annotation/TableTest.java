package test.annotation;

import com.usher.bean.User;
import com.usher.utils.ORMAnnotationUtil;

import java.lang.reflect.Field;

/**
 * @Author: Usher
 * @Description:
 */
public class TableTest {
    public static void main(String[] args) {
        String tableName = ORMAnnotationUtil.getTableName(User.class);
        System.out.println(tableName);

        Field[] fields = User.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(ORMAnnotationUtil.getColumnName(field));
        }
    }
}
