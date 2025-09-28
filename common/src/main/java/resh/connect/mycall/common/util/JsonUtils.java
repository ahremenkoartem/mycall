package resh.connect.mycall.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Преобразование любого объекта в JSON строку
    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    // Парсинг JSON строки в объект указанного класса
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    // Парсинг JSON строки в Map для универсального разбора
    public static <K, V> java.util.Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(java.util.Map.class, keyClass, valueClass));
    }
}
