package com.example.permmenu.dto;

import lombok.Data;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Data
public class ResultVO<T> {

    /** 响应码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     */
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ResultVO<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> ResultVO<T> error(int code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（默认 500）
     */
    public static <T> ResultVO<T> error(String message) {
        return error(500, message);
    }
}
