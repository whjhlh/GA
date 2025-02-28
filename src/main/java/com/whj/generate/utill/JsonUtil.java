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
    static {
        SERIALIZER_FEATURES=new SerializerFeature[]{SerializerFeature.WriteDateUseDateFormat};
    }
    public static String toJson(final Object object){
        return JSON.toJSONString(object,SERIALIZER_FEATURES);
    }

}
