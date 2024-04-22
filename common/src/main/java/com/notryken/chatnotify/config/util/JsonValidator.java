package com.notryken.chatnotify.config.util;

import com.google.gson.JsonParseException;
import com.notryken.chatnotify.ChatNotify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class JsonValidator<T> {
    /**
     * Validates a deserialized object.
     * Source: <a href="https://stackoverflow.com/a/21634867">StackOverflow </a>
     * @param obj the object to validate
     * @return the validated object
     * @throws JsonParseException if any field of the object is {@code null}
     */
    public T validateNonNull(T obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(JsonRequired.class) != null) {
                try {
                    field.setAccessible(true);
                    if (field.get(obj) == null) {
                        throw new JsonParseException("Missing field in JSON: " + field.getName());
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    ChatNotify.LOG.warn(e.getMessage());
                }
            }
        }
        return obj;
    }
}
