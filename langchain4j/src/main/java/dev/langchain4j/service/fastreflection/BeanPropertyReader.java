package dev.langchain4j.service.fastreflection;

public interface BeanPropertyReader {

    Object executeMethod(Object bean, String methodName, Object... args);

}
