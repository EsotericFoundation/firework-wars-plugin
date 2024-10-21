package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public final class ReflectUtil {
    private boolean reflecting;
    private ComponentLogger logger;

    private Class<?> clazz;
    private Object instance;

    public void useLogger(ComponentLogger logger) {
        this.logger = logger;
    }

    public Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            logger.error("Failed to get field:" + e.getMessage());
        }

        throw new RuntimeException("Failed to get field!");
    }

    public Field getField(String fieldName) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot get field without class context outside a reflection function!");
        }

        return getField(clazz, fieldName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(Field field, Object instance) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            logger.error("Failed to get field value:" + e.getMessage());
        }

        throw new RuntimeException("Failed to get field value!");
    }

    public <T> T getFieldValue(Field field) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot get field value without class context outside a reflection function!");
        }

        return getFieldValue(field, instance);
    }

    public <T> T getFieldValue(Class<?> clazz, String fieldName, Object instance) {
        Field field = getField(clazz, fieldName);
        return getFieldValue(field, instance);
    }

    public <T> T getFieldValue(String fieldName) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot get field value without class context outside a reflection function!");
        }

        return getFieldValue(clazz, fieldName, instance);
    }

    public void setFieldValue(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            logger.error("Failed to set field value:" + e.getMessage());
        }
    }

    public void setFieldValue(Field field, Object value) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot set field value without class context outside a reflection function!");
        }

        setFieldValue(field, instance, value);
    }

    public void setFieldValue(Class<?> clazz, String fieldName, Object instance, Object value) {
        Field field = getField(clazz, fieldName);
        setFieldValue(field, instance, value);
    }

    public void setFieldValue(String fieldName, Object value) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot set field value without class context outside a reflection function!");
        }

        setFieldValue(clazz, fieldName, instance, value);
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            logger.error("Failed to get method:" + e.getMessage());
        }

        throw new RuntimeException("Failed to get method!");
    }

    public Method getMethod(String methodName, Class<?>... parameterTypes) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot get method without class context outside a reflection function!");
        }

        return getMethod(clazz, methodName, parameterTypes);
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeMethod(Method method, Object instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            logger.error("Failed to invoke method:" + e.getMessage());
        }

        throw new RuntimeException("Failed to invoke method!");
    }

    public <T> T invokeMethod(Method method, Object... args) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot invoke method without class context outside a reflection function!");
        }

        return invokeMethod(method, instance, args);
    }

    public <T> T invokeMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object instance, Object... args) {
        Method method = getMethod(clazz, methodName, parameterTypes);
        return invokeMethod(method, instance, args);
    }

    public <T> T invokeMethod(String methodName, Class<?>[] parameterTypes, Object... args) {
        if (!reflecting) {
            throw new IllegalStateException("Cannot invoke method without class context outside a reflection function!");
        }

        return invokeMethod(clazz, methodName, parameterTypes, instance, args);
    }

    public void reflect(Class<?> clazz, Object instance, Runnable runnable) {
        reflecting = true;
        this.clazz = clazz;
        this.instance = instance;

        runnable.run();

        reflecting = false;
        this.clazz = null;
        this.instance = null;
    }

    public <T> T reflect(Class<?> clazz, Object instance, Supplier<T> supplier) {
        reflecting = true;
        this.clazz = clazz;
        this.instance = instance;

        T value = supplier.get();

        reflecting = false;
        this.clazz = null;
        this.instance = null;

        return value;
    }
}
