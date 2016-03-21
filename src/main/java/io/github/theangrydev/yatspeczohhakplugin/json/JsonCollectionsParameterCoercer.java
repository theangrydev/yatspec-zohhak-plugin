package io.github.theangrydev.yatspeczohhakplugin.json;

import com.googlecode.zohhak.api.backend.ParameterCoercer;
import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

class JsonCollectionsParameterCoercer implements ParameterCoercer {

    private final ParameterCoercer defaultParameterCoercer;

    JsonCollectionsParameterCoercer(ParameterCoercer defaultParameterCoercer) {
        this.defaultParameterCoercer = defaultParameterCoercer;
    }

    @Override
    public Object coerceParameter(Type type, String stringToParse) {
        try {
            if (stringToParse == null) {
                return null;
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (rawType instanceof Class) {
                    Class<?> rawClass = (Class<?>) rawType;
                    if (rawClass == List.class || rawClass == Iterable.class || rawClass == Collection.class) {
                        return coerceCollection(stringToParse, rawType, actualTypeArguments[0], ListBuilder.INSTANCE);
                    } else if (rawClass == Set.class) {
                        return coerceCollection(stringToParse, rawType, actualTypeArguments[0], SetBuilder.INSTANCE);
                    } else if (rawClass == Map.class) {
                        return coerceMap(stringToParse, rawType, actualTypeArguments[0], actualTypeArguments[1]);
                    } else {
                        return coerceParameter(rawType, stringToParse);
                    }
                }
            } else if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                Result execution = tryToCoerceCollection(stringToParse, genericArrayType.getGenericComponentType(), ArrayBuilder.INSTANCE);
                if (execution.succeeded()) {
                    return execution.getResult();
                }
            } else if (type instanceof Class) {
                Class<?> targetType = ClassUtils.primitiveToWrapper((Class<?>) type);
                if (targetType.isArray()) {
                    Result execution = tryToCoerceCollection(stringToParse, targetType.getComponentType(), ArrayBuilder.INSTANCE);
                    if (execution.succeeded()) {
                        return execution.getResult();
                    }
                }
                return defaultParameterCoercer.coerceParameter(targetType, stringToParse);
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(coercingExceptionMessage(stringToParse, type), e);
        }
        throw new IllegalArgumentException(coercingExceptionMessage(stringToParse, type));
    }

    private String coercingExceptionMessage(String stringToParse, Type targetType) {
        return format("Cannot interpret String '%s' as a '%s'", stringToParse, targetType);
    }

    private Object coerceMap(String stringToParse, Type rawType, Type keyType, Type valueType) {
        Result execution = tryToCoerceMap(stringToParse, keyType, valueType);
        if (execution.succeeded()) {
            return execution.getResult();
        } else {
            // can't find a coercion for the element type, so try the raw type of the whole collection instead
            return coerceParameter(rawType, stringToParse);
        }
    }

    private Object coerceCollection(String stringToParse, Type rawType, Type actualTypeArgument, CollectionBuilder collectionBuilder) {
        Result execution = tryToCoerceCollection(stringToParse, actualTypeArgument, collectionBuilder);
        if (execution.succeeded()) {
            return execution.getResult();
        } else {
            // can't find a coercion for the element type, so try the raw type of the whole Map instead
            return coerceParameter(rawType, stringToParse);
        }
    }

    private Result tryToCoerceMap(String stringToParse, Type keyType, Type valueType) {
        try {
            JSONObject jsonObject = new JSONObject(stringToParse);
            int size = jsonObject.length();
            Object map = MapBuilder.INSTANCE.newMap(size);
            for (String jsonKey : jsonObject.keySet()) {
                String jsonValue = jsonObject.get(jsonKey).toString();
                Object key = coerceParameter(keyType, jsonKey);
                Object value = coerceParameter(valueType, jsonValue);
                MapBuilder.INSTANCE.addElement(map, key, value);
            }
            return Result.success(map);
        } catch (RuntimeException e) {
            return Result.FAILURE;
        }
    }

    private Result tryToCoerceCollection(String stringToParse, Type elementType, CollectionBuilder collectionBuilder) {
        try {
            JSONArray jsonArray = new JSONArray(stringToParse);
            int size = jsonArray.length();
            Object collection = collectionBuilder.newCollection(elementType, size);
            for (int index = 0; index < size; index++) {
                Object element = coerceParameter(elementType, jsonArray.get(index).toString());
                collectionBuilder.setElement(collection, index, element);
            }
            return Result.success(collection);
        } catch (RuntimeException e) {
            return Result.FAILURE;
        }
    }
}
