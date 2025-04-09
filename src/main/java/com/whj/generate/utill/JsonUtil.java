package com.whj.generate.utill;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author whj
 * @date 2025-03-29 下午6:25
 */
public class JsonUtil {
    /**
     * 序列化配置
     */
    private static final SerializerFeature[] SERIALIZER_FEATURES;

    /**
     * 格式化后的序列化配置
     */
    private static final SerializerFeature[] FORMAT_SERIALIZER_FEATURES;

    static {
        SERIALIZER_FEATURES = new SerializerFeature[]{SerializerFeature.WriteDateUseDateFormat};
        FORMAT_SERIALIZER_FEATURES = new SerializerFeature[]{
                SerializerFeature.PrettyFormat,       // 格式化输出
                SerializerFeature.WriteDateUseDateFormat, // 日期格式化
                SerializerFeature.WriteMapNullValue,  // 保留null字段
                SerializerFeature.WriteNullListAsEmpty // 空列表返回[]
        };
    }

    public static String toJson(final Object object) {
        return JSON.toJSONString(object, SERIALIZER_FEATURES);
    }
    public static String toFormatJson(final Object object) {
        return JSON.toJSONString(object, FORMAT_SERIALIZER_FEATURES);
    }

}
