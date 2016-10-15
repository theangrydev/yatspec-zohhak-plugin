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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static java.lang.String.format;

class JsonCollectionsParameterCoercer implements ParameterCoercer {

    private final ParameterCoercer defaultParameterCoercer;

    JsonCollectionsParameterCoercer(ParameterCoercer defaultParameterCoercer) {
        this.defaultParameterCoercer = defaultParameterCoercer;
    }

    @Override
    public Object coerceParameter(Type type, String stringToParse) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            Class<?> rawClass = (Class<?>) rawType;
            if (rawClass == List.class) {
                return coerceCollection(stringToParse, actualTypeArguments[0], (actualTypeArgument, size) -> new ListBuilder(size));
            } else if (rawClass == Set.class) {
                return coerceCollection(stringToParse, actualTypeArguments[0], (actualTypeArgument, size) -> new SetBuilder(size));
            } else if (rawClass == Map.class) {
                return coerceMap(stringToParse, actualTypeArguments[0], actualTypeArguments[1]);
            } else {
                return coerceParameter(rawType, stringToParse);
            }
        } else if (type instanceof Class) {
            Class<?> targetType = ClassUtils.primitiveToWrapper((Class<?>) type);
            if (targetType.isArray()) {
                return coerceCollection(stringToParse, targetType.getComponentType(), ArrayBuilder::new);
            }
            return defaultParameterCoercer.coerceParameter(targetType, stringToParse);
        } else {
            throw new IllegalArgumentException(format("Cannot interpret '%s' as a '%s'", stringToParse, type));
        }
    }

    private Map<Object, Object> coerceMap(String stringToParse, Type keyType, Type valueType) {
        JSONObject jsonObject = new JSONObject(stringToParse);
        int size = jsonObject.length();
        Map<Object, Object> map = new HashMap<>(size);
        for (String jsonKey : jsonObject.keySet()) {
            String jsonValue = jsonObject.get(jsonKey).toString();
            Object key = coerceParameter(keyType, jsonKey);
            Object value = coerceParameter(valueType, jsonValue);
            map.put(key, value);
        }
        return map;
    }

    private Object coerceCollection(String stringToParse, Type actualTypeArgument, BiFunction<Type, Integer, CollectionBuilder> collectionBuilderConstructor) {
        JSONArray jsonArray = new JSONArray(stringToParse);
        int size = jsonArray.length();
        CollectionBuilder collectionBuilder = collectionBuilderConstructor.apply(actualTypeArgument, size);
        for (int index = 0; index < size; index++) {
            Object element = coerceParameter(actualTypeArgument, jsonArray.get(index).toString());
            collectionBuilder.add(element);
        }
        return collectionBuilder.build();
    }
}
