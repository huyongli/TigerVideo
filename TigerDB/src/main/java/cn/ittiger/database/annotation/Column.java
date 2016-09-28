package cn.ittiger.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体字段不设置该注解，则默认已字段名作为列名
 * 此注解配置数据库指定列名和字段默认值，不配置columnName则默认以字段名作为列名，不配置defaultValue则无列默认值
 * @author: huylee
 * @time:	2015-8-13下午10:37:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * 配置该字段映射到数据库中的列名，不配置默认为字段名
	 * @author: huylee
	 * @time:	2015-8-13下午10:33:14
	 * @return
	 */
	public String columnName() default "";
	/**
	 * 字段默认值
	 * @author: huylee
	 * @time:	2015-8-13下午10:39:36
	 * @return
	 */
	public String defaultValue() default "";
}
