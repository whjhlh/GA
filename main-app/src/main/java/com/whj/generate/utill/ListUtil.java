package com.whj.generate.utill;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author whj
 * @date 2025-05-19 下午3:25
 */
public class ListUtil {
    /**
     * 默认newlist
     */
    public static <T> List<T> defualtList(List<T> list) {
        if(list==null|| list.isEmpty()){
            return Lists.newArrayList();
        }
        return list;
    }

}
