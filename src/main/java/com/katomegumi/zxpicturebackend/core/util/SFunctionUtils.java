package com.katomegumi.zxpicturebackend.core.util;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.ReflectUtils;

import java.beans.PropertyDescriptor;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Megumi
 * @description :Mybatis-plus SFunction工具类
 * @createDate : 2025/5/28 上午11:55
 */
@UtilityClass
public class SFunctionUtils {

    public static final Map<Class<?>, PropertyDescriptor[]> cache = new HashMap<>();

    /**
     * 获取类中的 SFunction
     *
     * @param clazz 类
     * @param prop  类中的属性名称
     * @return SFunction
     */
    public static <T> SFunction<T, ?> getSFunction(Class<T> clazz, String prop) {
        try {
            PropertyDescriptor[] beanGetters;
            if (cache.containsKey(clazz)) {
                beanGetters = cache.get(clazz);
            } else {
                beanGetters = ReflectUtils.getBeanGetters(clazz);
                cache.put(clazz, beanGetters);
            }
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Optional<PropertyDescriptor> optional = Arrays.stream(beanGetters)
                    .filter(pd -> pd.getName().equals(prop))
                    .findFirst();
            if (optional.isPresent()) {
                // 反射获取getter方法
                Method readMethod = optional.get().getReadMethod();
                // 拿到方法句柄
                final MethodHandle methodHandle = lookup.unreflect(readMethod);
                // 创建动态调用链
                CallSite callSite = LambdaMetafactory.altMetafactory(
                        lookup,
                        "apply",
                        MethodType.methodType(SFunction.class),
                        MethodType.methodType(Object.class, Object.class),
                        methodHandle,
                        MethodType.methodType(readMethod.getReturnType(), clazz),
                        LambdaMetafactory.FLAG_SERIALIZABLE
                );
                return (SFunction<T, ?>) callSite.getTarget().invokeExact();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}

