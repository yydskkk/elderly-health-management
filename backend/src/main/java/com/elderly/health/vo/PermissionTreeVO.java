package com.elderly.health.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限树形 VO
 *
 * @author elderly-health
 */
@Data
public class PermissionTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 类型：1菜单2按钮
     */
    private Integer type;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子权限列表
     */
    private List<PermissionTreeVO> children;
}
