package com.whj.generate.utill;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author whj
 * @date 2025-03-30 下午6:39
 */
public class FileUtil {
    /**
     * 获取类编译后的路径
     */
    static File getClassLocation(Class<?> clazz) throws IOException {
        String resource= clazz.getName().replace(".", "/")+".class";
        URL url = clazz.getClassLoader().getResource(resource);
        String decodedPath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
        File dir = new File(decodedPath);
        System.out.println(" 类文件路径: " + dir.getAbsolutePath());
        return dir;
    }
}
