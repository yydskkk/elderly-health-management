package com.elderly.health.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.elderly.health.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充 createTime、updateTime、createBy、updateBy 字段
 *
 * @author elderly-health
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 填充字段：createTime、updateTime、createBy、updateBy
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     * 填充字段：updateTime、updateBy
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    /**
     * 获取当前登录用户ID
     * 未登录时返回 null
     *
     * @return 当前用户ID
     */
    private String getCurrentUser() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            return Objects.isNull(userId) ? null : userId.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
