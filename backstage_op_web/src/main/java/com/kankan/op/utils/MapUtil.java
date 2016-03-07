package com.kankan.op.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 负责构造Map对象时key-value对的检查和填充
 * 
 * @author ZengDong
 * @since 2010-11-24 下午06:46:38
 */
public class MapUtil {

    /**
     * 检查key-value对是否对应
     * 
     * @param keyvalue 若干个key-value对
     */
    public static void checkKeyValueLength(Object... keyvalue) {
        if (keyvalue.length % 2 != 0) {
            throw new IllegalArgumentException("keyvalue.length is invalid:" + keyvalue.length);
        }
    }

    /**
     * 将若干个key-value对放入指定的map对象
     * 
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map 要放入的Map对象
     * @param keyvalue 若干个key-value对
     * @return map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> buildMap(Map<K, V> map, Object... keyvalue) {
        checkKeyValueLength(keyvalue);
        for (int i = 0; i < keyvalue.length; i++) {
            map.put((K) keyvalue[i++], (V) keyvalue[i]);
        }
        return map;
    }

    public static void main(String[] args) {
        String str = "a,,b,fuck,3,v";
        Object[] fieldAndValue = str.split(",");
        Map<String, Object> optionMap = MapUtil.buildMap(new HashMap<String, Object>(), fieldAndValue);
        for (Map.Entry<String, Object> entry : optionMap.entrySet()) {
            System.err.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
