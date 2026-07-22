package com.elderly.health.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解，标注需要的角色
 *
 * @author elderly-health
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /**
     * 需要的角色编码列表（满足其一即可）
     *
     * @return 角色编码数组
     */
    String[] value();
}
