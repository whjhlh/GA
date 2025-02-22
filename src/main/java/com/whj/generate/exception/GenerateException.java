package com.whj.generate.exception;

/**
 * @author whj
 * @date 2025-02-22 下午11:43
 */
public class GenerateException extends RuntimeException{
    /** serialVersionUID */
    private static final long serialVersionUID = 1238761234059563L;

    /** 错误码 */
    private GenerateErrorEnum code;

    public GenerateException() {
        super();
        code=GenerateErrorEnum.UNKNOWN_EXCEPTION;
    }
    public GenerateException(final GenerateErrorEnum generateErrorEnum,final String errorMsg) {
        super(errorMsg);
        this.code = generateErrorEnum;
    }

    public GenerateException(final GenerateErrorEnum generateErrorEnum, final String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.code = generateErrorEnum;
    }

    public GenerateErrorEnum getCode() {
        return code;
    }

    public void setCode(GenerateErrorEnum code) {
        this.code = code;
    }
}
