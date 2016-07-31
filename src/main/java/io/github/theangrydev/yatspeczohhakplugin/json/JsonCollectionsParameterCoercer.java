/*
 * Copyright 2016 Liam Williams <liam.williams@zoho.com>.
 *
 * This file is part of yatspec-zohhak-plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

@SuppressWarnings("PMD") // TODO: tidy this up
class JsonCollectionsParameterCoercer implements ParameterCoercer {

    private final ParameterCoercer defaultParameterCoercer;
    private final ListBuilder listBuilder;
    private final SetBuilder setBuilder;
    private final ArrayBuilder arrayBuilder;
    private final MapBuilder mapBuilder;

    JsonCollectionsParameterCoercer(ParameterCoercer defaultParameterCoercer, ListBuilder listBuilder, SetBuilder setBuilder, ArrayBuilder arrayBuilder, MapBuilder mapBuilder) {
        this.defaultParameterCoercer = defaultParameterCoercer;
        this.listBuilder = listBuilder;
        this.setBuilder = setBuilder;
        this.arrayBuilder = arrayBuilder;
        this.mapBuilder = mapBuilder;
    }

    @Override
    public Object coerceParameter(Type type, String stringToParse) {
        try {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (rawType instanceof Class) {
                    Class<?> rawClass = (Class<?>) rawType;
                    if (rawClass == List.class || rawClass == Iterable.class || rawClass == Collection.class) {
                        return coerceCollection(stringToParse, rawType, actualTypeArguments[0], listBuilder);
                    } else if (rawClass == Set.class) {
                        return coerceCollection(stringToParse, rawType, actualTypeArguments[0], setBuilder);
                    } else if (rawClass == Map.class) {
                        return coerceMap(stringToParse, rawType, actualTypeArguments[0], actualTypeArguments[1]);
                    } else {
                        return coerceParameter(rawType, stringToParse);
                    }
                }
            } else if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                Result execution = tryToCoerceCollection(stringToParse, genericArrayType.getGenericComponentType(), arrayBuilder);
                if (execution.succeeded) {
                    return execution.value;
                }
            } else if (type instanceof Class) {
                Class<?> targetType = ClassUtils.primitiveToWrapper((Class<?>) type);
                if (targetType.isArray()) {
                    Result execution = tryToCoerceCollection(stringToParse, targetType.getComponentType(), arrayBuilder);
                    if (execution.succeeded) {
                        return execution.value;
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
        if (execution.succeeded) {
            return execution.value;
        } else {
            // can't find a coercion for the element type, so try the raw type of the whole collection instead
            return coerceParameter(rawType, stringToParse);
        }
    }

    private Object coerceCollection(String stringToParse, Type rawType, Type actualTypeArgument, CollectionBuilder collectionBuilder) {
        Result execution = tryToCoerceCollection(stringToParse, actualTypeArgument, collectionBuilder);
        if (execution.succeeded) {
            return execution.value;
        } else {
            // can't find a coercion for the element type, so try the raw type of the whole Map instead
            return coerceParameter(rawType, stringToParse);
        }
    }

    private Result tryToCoerceMap(String stringToParse, Type keyType, Type valueType) {
        try {
            JSONObject jsonObject = new JSONObject(stringToParse);
            int size = jsonObject.length();
            Object map = mapBuilder.newMap(size);
            for (String jsonKey : jsonObject.keySet()) {
                String jsonValue = jsonObject.get(jsonKey).toString();
                Object key = coerceParameter(keyType, jsonKey);
                Object value = coerceParameter(valueType, jsonValue);
                mapBuilder.addElement(map, key, value);
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
