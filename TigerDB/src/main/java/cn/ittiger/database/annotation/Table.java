package cn.ittiger.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名注解，实体不设置此注解，或者设置了此注解但name不设置则默认以实体类名作为表名
 * Author: hyl
 * Time: 2015-8-14下午9:17:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
	/**
	 * 自定义表名
	 * Author: hyl
	 * Time: 2015-8-14下午9:18:12
	 * @return
	 */
	public String name() default "";
}
