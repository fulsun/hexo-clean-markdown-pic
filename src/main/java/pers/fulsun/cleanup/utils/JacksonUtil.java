package pers.fulsun.cleanup.utils;

/**
 * Jackson工具类
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 注册Java 8时间模块，以便正确处理LocalDate, LocalDateTime等
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用日期转换为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 要转换的对象
     * @return JSON字符串
     * @throws JsonProcessingException 如果转换失败
     */
    public static String toJson(Object obj)  {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json  JSON字符串
     * @param clazz 目标对象的类
     * @param <T>   目标对象的类型
     * @return 转换后的对象
     * @throws JsonProcessingException 如果转换失败
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 将对象转换为格式化的JSON字符串（美化输出）
     *
     * @param obj 要转换的对象
     * @return 格式化后的JSON字符串
     * @throws JsonProcessingException 如果转换失败
     */
    public static String toPrettyJson(Object obj) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
}