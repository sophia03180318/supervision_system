package com.jcca.util;


import com.jcca.common.ResultVo;

/**
 * 响应数据(结果)最外层对象工具
 *
 * @author hanwone
 * @date 2018/10/15
 */
public class ResultVoUtil {

    public static ResultVo<String> SAVE_SUCCESS = success("保存成功");

    public static ResultVo<String> REMOVE_SUCCESS = success("删除成功");

    public static ResultVo<String> CREATE_SUCCESS = success("创建成功");

    /**
     * 操作成功
     *
     * @param object 对象
     */
    public static <T> ResultVo<T> outSucess(T object) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setCode(ResultEnum.OUT_SUCCESS.getCode());
        resultVo.setMsg(ResultEnum.OUT_SUCCESS.getMessage());
        resultVo.setData(object);
        return resultVo;
    }

    /**
     * 操作成功
     *
     * @param msg    提示信息
     * @param object 对象
     */
    public static <T> ResultVo<T> success(String msg, T object) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setMsg(msg);
        resultVo.setCode(ResultEnum.SUCCESS.getCode());
        resultVo.setData(object);
        return resultVo;
    }

    /**
     * 操作成功，使用默认的提示信息
     *
     * @param object 对象
     */
    public static <T> ResultVo<T> success(T object) {
        String message = ResultEnum.SUCCESS.getMessage();
        return success(message, object);
    }

    /**
     * 操作成功，返回提示信息，不返回数据
     */
    public static <T> ResultVo<T> success(String msg) {
        return success(msg, null);
    }

    /**
     * 操作成功，不返回数据
     */
    public static ResultVo success() {
        return success("成功");
    }

    /**
     * 操作有误
     *
     * @param code 错误码
     * @param msg  提示信息
     */
    public static ResultVo error(Integer code, String msg) {
        ResultVo resultVo = new ResultVo();
        resultVo.setMsg(msg);
        resultVo.setCode(code);
        return resultVo;
    }


    /**
     * 参数错误
     *
     * @param msg 提示信息
     */
    public static <T> ResultVo<T> paramError(String msg, Class<T> t) {
        ResultVo<T> resultVo = new ResultVo<T>();
        resultVo.setMsg(msg);
        resultVo.setCode(ResultEnum.PARAM_ERROR.getCode());
        return resultVo;
    }

    /**
     * 操作有误
     *
     * @param msg 提示信息
     */
    public static ResultVo error(String msg) {
        Integer code = ResultEnum.ERROR.getCode();
        return error(code, msg);
    }

    /**
     * 警告提示
     *
     * @param msg
     * @return
     */
    public static ResultVo warn(String msg) {
        Integer code = ResultEnum.WARNING.getCode();
        return error(code, msg);
    }

    /**
     * 操作有误，只返回默认错误状态码
     */
    public static ResultVo error() {
        return error(null);
    }

    /**
     * 操作失败 返回失败数据
     *
     * @param code   响应码
     * @param msg    提示信息
     * @param object 对象
     */
    public static <T> ResultVo<T> error(Integer code, String msg, T object) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setMsg(msg);
        resultVo.setCode(code);
        resultVo.setData(object);
        return resultVo;
    }

}
