package me.dingtou.result;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ken
 */
@Data
public class AppResult<T> implements Serializable {

    private static final long serialVersionUID = -6692466994829419961L;
    private T data;
    private boolean success;
    private String errorCode;
    private String message;

    public AppResult() {
    }

    public static <T> AppResult<T> success(T data, String message) {
        AppResult<T> success = success(data);
        success.message = message;
        return success;
    }

    public static <T> AppResult<T> success(T data) {
        if (data == null) {
            return AppResult.success();
        }
        if (!(data instanceof Serializable)) {
            throw new RuntimeException(
                    String.format("class %s must implements java.io.Serializable", data.getClass().getName()));
        }
        AppResult<T> TcGpResult = new AppResult<T>();
        TcGpResult.success = true;
        TcGpResult.message = "OK";
        TcGpResult.data = data;
        return TcGpResult;
    }

    public static <T> AppResult<T> success() {
        AppResult<T> result = new AppResult<T>();
        result.success = true;
        result.message = "OK";
        result.data = null;
        return result;
    }

    public static <T> AppResult<T> fail() {
        AppResult<T> result = new AppResult<>();
        result.success = false;
        result.errorCode = "";
        result.message = "";
        return result;
    }

    public static <T> AppResult<T> fail(String errorCode, String errorMessage) {
        AppResult<T> result = new AppResult<T>();
        result.success = false;
        result.errorCode = errorCode;
        result.message = errorMessage;
        return result;
    }

    public static <T> AppResult<T> fail(T data, String errorCode, String errorMessage) {
        AppResult<T> result = new AppResult<T>();
        result.success = false;
        result.data = data;
        result.errorCode = errorCode;
        result.message = errorMessage;
        return result;
    }


    public boolean getSuccess() {
        return success;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return !isSuccess();
    }
}
