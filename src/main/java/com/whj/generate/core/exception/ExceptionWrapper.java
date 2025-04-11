package com.whj.generate.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param callable
     * @param generateErrorEnum
     * @param errMsgFormat
     * @param formatArgs
     * @return
     */
    public static <R> R process(Callable<R> callable, GenerateErrorEnum generateErrorEnum, final String errMsgFormat, final Object... formatArgs){
        try {
            return callable.call();
        } catch (GenerateException e) {
            throw e;
        } catch (Throwable e) {
            throw new GenerateException(generateErrorEnum, String.format(errMsgFormat, formatArgs), e);
        }
    }

    /**
     * 运行时异常包装-只打日志
     * @param runnable
     * @param errMsgFormat
     * @param formatArgs
     */
    public static void processSafe(Runnable runnable, String errMsgFormat , final Object... formatArgs){
        try {
            runnable.run();
        } catch (Throwable e) {
            logger.warn("runnable error:{}", String.format(errMsgFormat, formatArgs), e);
        }

    }
}

