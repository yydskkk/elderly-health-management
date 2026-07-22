package com.elderly.health.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 标注在 Controller 方法上，用于记录操作日志
 *
 * @author elderly-health
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述
     *
     * @return 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     * 1-查询 2-新增 3-修改 4-删除 5-导出 6-导入
     *
     * @return 操作类型
     */
    int type() default 1;
}
