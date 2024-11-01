package dev.langchain4j.service.tool;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

public class ByteBuddyToolExecutor implements ToolExecutor {

    private final Object object;

    public ByteBuddyToolExecutor(Object object) {
            this.object = object;
    }



    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        try {
            Class<?> dynamicClass = new ByteBuddy()
                .subclass(Object.class)
                .name("DynamicExecutor")
                .method(ElementMatchers.named(toolExecutionRequest.name()))
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {

                    if (method.getName().equals(toolExecutionRequest.name())) {
                        String[] argArray = toolExecutionRequest.arguments().split(",");
                        return method.invoke(object, (Object[]) argArray);
                    }
                    return null;
                }))
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

            Object executorInstance = dynamicClass.getDeclaredConstructor().newInstance();

            return "Executed tool method: " + toolExecutionRequest.name();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing tool: " + e.getMessage();
        }
    }

    private Class<?>[] getParameterTypes(Object[] parameters) {
        return java.util.Arrays.stream(parameters)
            .map(param -> param != null ? param.getClass() : Object.class)
            .toArray(Class<?>[]::new);
    }


}
