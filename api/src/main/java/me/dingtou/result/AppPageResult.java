package me.dingtou.result;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @author ken
 */
@Data
public class AppPageResult<T> implements Serializable {
    private static final long serialVersionUID = 3398518678970845452L;

    public static final int MAX_PAGE_SIZE = 50;

    private List<T> data;
    private boolean success;
    private String errorCode;
    private String message;
    /**
     * 当前分页	，从1开始
     */
    private int current;
    /**
     * 页面大小
     */
    private int pageSize;
    /**
     * 总数
     */
    private long total;

    public AppPageResult() {
    }

    public static <T> AppPageResult<T> success(List<T> data) {
        if (data == null) {
            return AppPageResult.success();
        }
        if (!(data instanceof Serializable)) {
            throw new RuntimeException(
                String.format("class %s must implements java.io.Serializable", data.getClass().getName()));
        }
        AppPageResult<T> pageResult = new AppPageResult<T>();
        pageResult.success = true;
        pageResult.message = "OK";
        pageResult.data = data;
        pageResult.total = data.size();
        return pageResult;
    }


    public static <T> AppPageResult<T> success() {
        AppPageResult<T> pageResult = new AppPageResult<T>();
        pageResult.success = true;
        pageResult.message = "OK";
        pageResult.data = null;
        pageResult.total = 0;
        return pageResult;
    }

    public static <T> AppPageResult<T> fail(String errorCode, String errorMessage) {
        AppPageResult<T> pageResult = new AppPageResult<T>();
        pageResult.success = false;
        pageResult.errorCode = errorCode;
        pageResult.message = errorMessage;
        return pageResult;
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
