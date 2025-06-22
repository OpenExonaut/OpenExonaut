package xyz.openexonaut.extension.exolib.utils;

import java.lang.reflect.*;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.resources.*;

public final class ExoParamUtils {
    private ExoParamUtils() {}

    public static <T> T deserializeField(ISFSObject params, String key, Class<T> clazz) {
        SFSDataWrapper wrapper = params.get(key);
        if (wrapper != null) {
            Object object = wrapper.getObject();
            if (clazz.isInstance(object)) {
                return clazz.cast(object);
            }
        }
        return null;
    }

    // behavior guaranteed consistent only if clazz has only one non-inherited constructor
    // such constructors must be public and use the primitive wrappers instead of primitives
    public static <T> T deserialize(ISFSObject params, Class<T> clazz) {
        @SuppressWarnings("unchecked") // only needed because of Java array shenanigans
        Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructors()[0];

        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            String paramName = param.getName();

            SFSDataWrapper wrapper = params.get(paramName);
            if (wrapper == null) {
                if (ExoProps.getInputDebug()) {
                    throw new RuntimeException(String.format("param %s not found", paramName));
                }
                return null;
            }

            Object object = wrapper.getObject();
            if (object == null || paramType.isInstance(object)) {
                values[i] = object;
            } else {
                if (ExoProps.getInputDebug()) {
                    throw new RuntimeException(
                            String.format(
                                    "param %s type (%s) not match %s",
                                    paramName, paramType, object.getClass()));
                }
                return null;
            }
        }

        try {
            return constructor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // update if needing to handle additional field types. handles int, float
    public static ISFSObject serialize(Object toSerialize, int playerId) {
        ISFSObject serializedObject = new SFSObject();

        for (Field field : toSerialize.getClass().getDeclaredFields()) {
            Object value = null;
            try {
                value = field.get(toSerialize);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            if (value instanceof Integer) {
                serializedObject.putInt(field.getName(), (Integer) value);
            } else if (value instanceof Float) {
                serializedObject.putFloat(field.getName(), (Float) value);
            } else {
                throw new RuntimeException(
                        String.format(
                                "unhandled serialize field name %s type %s",
                                field.getName(), field.getType()));
            }
        }

        serializedObject.putInt("playerId", playerId);
        return serializedObject;
    }
}
