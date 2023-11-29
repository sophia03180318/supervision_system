package com.jcca.util;

import lombok.Getter;

/**
 * 后台返回结果集枚举
 *
 * @author hanwone
 * @date 2018/8/14
 */
@Getter
public enum ResultEnum {

    /**
     * 通用状态
     */
    SUCCESS(200, "请求成功"),
    ERROR(999, "系统错误,请联系管理员"),
    WARNING(333, "操作错误"),

    /**
     * 账户问题
     */
    USER_EXIST_ERROR(900, "该用户名不存在"),
    USER_EXIST(901, "该用户名已经存在"),
    USER_INEQUALITY(902, "两次密码不一致"),
    USER_OLD_PWD_ERROR(903, "原密码不正确"),
    USERNAME_PWD_NULL(904, "用户名或密码不能为空"),
    USER_CAPTCHA_ERROR(905, "验证码错误"),
    USERNAME_PWD_ERROR(906, "用户名或密码错误"),
    ACCOUNT_FREEZED(907, "账户被冻结"),
    TOKEN_EXPIRY(908, "TOKEN过期"),
    MANY_LOGIN(909, "账号已在其他地方登录"),

    /**
     * 角色问题
     */
    ROLE_EXIST(910, "该角色标识不能重复！"),
    NOT_ADMIN(911, "不是后台管理员！"),

    /**
     * 组织问题
     */
    ORG_EXIST_USER(920, "组织存在用户，无法删除"),

    /**
     * 字典问题
     */
    DICT_EXIST(930, "该字典标识不能重复！"),

    /**
     * 非法操作
     */
    STATUS_ERROR(940, "非法操作"),

    /**
     * 参数问题
     */
    PARAM_ERROR(960, "参数错误"),
    TIME_OUT_ERROR(961, "连接超时"),

    /**
     * 权限问题
     */
    NO_PERMISSIONS(950, "权限不足！"),
    NO_ADMIN_AUTH(951, "不允许操作超级管理员"),
    NO_ADMIN_STATUS(952, "不能修改超级管理员状态"),
    NO_ADMINROLE_AUTH(953, "不允许操作管理员角色"),

    /**
     * 外部接口问题
     */
    OUT_SUCCESS(0, "成功"),
    OUT_PARAM_LOST(1001, "缺少参数"),
    OUT_COLLECT_TEST_ERROR(1002, "采集指标测试未通过"),

    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
