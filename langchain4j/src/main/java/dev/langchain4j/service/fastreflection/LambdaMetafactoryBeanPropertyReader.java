package dev.langchain4j.service.fastreflection;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LambdaMetafactoryBeanPropertyReader {

    private final Map<String, Function<Object, Object>> noParamMethods = new HashMap<>();
    private final Map<String, Method> paramMethods = new HashMap<>();

    public LambdaMetafactoryBeanPropertyReader(Class<?> clazz) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Method method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length == 0) {
                try {
                    noParamMethods.put(methodName, createLambda(lookup, clazz, method));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to create lambda for method: " + methodName, e);
                }
            } else {
                method.setAccessible(true);
                paramMethods.put(methodName, method);
            }
        }
    }

    private Function<Object, Object> createLambda(MethodHandles.Lookup lookup, Class<?> clazz, Method method)
        throws LambdaConversionException, NoSuchMethodException, IllegalAccessException {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        CallSite site = LambdaMetafactory.metafactory(
            lookup,
            "apply",
            MethodType.methodType(Function.class),
            MethodType.methodType(Object.class, Object.class),
            lookup.findVirtual(clazz, methodName, MethodType.methodType(returnType)),
            MethodType.methodType(returnType, clazz)
        );

        try {
            return (Function<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to instantiate lambda for method: " + methodName, e);
        }
    }


    public Object executeMethod(Object bean, String methodName, Object... args) {
        if (args == null || args.length == 0) {
            return executeGetter(bean, methodName);
        }
        Method method = paramMethods.get(methodName);
        if (method == null) {
            throw new IllegalArgumentException("Method not found or does not accept parameters: " + methodName);
        }
        try {
            return method.invoke(bean, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to invoke method (" + method + ").", e);
        }
    }

    private Object executeGetter(Object bean, String methodName) {
        Function<Object, Object> function = noParamMethods.get(methodName);
        if (function == null) {
            throw new IllegalArgumentException("Method not found: " + methodName);
        }
        return function.apply(bean);
    }


}
