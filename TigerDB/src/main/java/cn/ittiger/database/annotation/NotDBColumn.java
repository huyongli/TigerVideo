package cn.ittiger.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识哪些字段不映射为数据库列
 * @author: huylee
 * @time:	2015-8-13下午10:36:05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotDBColumn {

}
