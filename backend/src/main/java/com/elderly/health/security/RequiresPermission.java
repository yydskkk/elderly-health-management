package com.elderly.health.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解，标注需要的权限
 *
 * @author elderly-health
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 需要的权限编码列表（满足其一即可）
     *
     * @return 权限编码数组
     */
    String[] value();
}
