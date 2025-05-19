package com.whj.generate.utill;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-05-19 下午3:16
 */
public class InputStreamUtil {

    /**
     * 获取类字节码流
     */
    public static InputStream getClassByteStream(Method method) {
        final String classResource = method.getDeclaringClass().getName().replace('.', '/') + ".class";
        return method.getDeclaringClass().getClassLoader().getResourceAsStream(classResource);
    }
}
