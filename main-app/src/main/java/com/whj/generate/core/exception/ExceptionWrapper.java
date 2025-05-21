package com.whj.generate.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * 异常包装类
 *
 * @author whj
 * @date 2025-02-22 下午11:38
 */
public class ExceptionWrapper {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(ExceptionWrapper.class);

    /**
     * 异常包装执行器
     *
     * @param callable
     * @param generateErrorEnum
     * @param errMsgFormat
     * @param formatArgs
     * @return
     */
    public static <R> R process(Callable<R> callable, GenerateErrorEnum generateErrorEnum, final String errMsgFormat, final Object... formatArgs) {
        try {
            return callable.call();
        } catch (GenerateException e) {
            throw e;
        } catch (Throwable e) {
            throw new GenerateException(generateErrorEnum, String.format(errMsgFormat, formatArgs), e);
        }
    }

    /**
     * 运行时异常包装-返回错误信息
     *
     * @param runnable
     * @param errMsgFormat
     * @param formatArgs
     */
    public static String processSafe(Runnable runnable, GenerateErrorEnum errorEnum, String errMsgFormat, Object... formatArgs) {
        try {
            runnable.run();
            return null;
        } catch (Throwable e) {
            // 提取原始异常信息
            Throwable cause = e;
            if (e instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) e).getTargetException();
            }
            String detailedMsg = String.format(errMsgFormat + "，原因：[%s:%s] %s",errorEnum.getDesc(),
                    Arrays.toString(formatArgs),
                    cause.getClass().getSimpleName(),
                    cause.getMessage());

            return detailedMsg;
        }
    }
}

