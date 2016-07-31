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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

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
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            Class<?> rawClass = (Class<?>) rawType;
            if (rawClass == List.class) {
                return coerceCollection(stringToParse, actualTypeArguments[0], listBuilder);
            } else if (rawClass == Set.class) {
                return coerceCollection(stringToParse, actualTypeArguments[0], setBuilder);
            } else if (rawClass == Map.class) {
                return coerceMap(stringToParse, actualTypeArguments[0], actualTypeArguments[1]);
            } else {
                return coerceParameter(rawType, stringToParse);
            }
        } else if (type instanceof Class) {
            Class<?> targetType = ClassUtils.primitiveToWrapper((Class<?>) type);
            if (targetType.isArray()) {
                return coerceCollection(stringToParse, targetType.getComponentType(), arrayBuilder);
            }
            return defaultParameterCoercer.coerceParameter(targetType, stringToParse);
        } else {
            throw new IllegalArgumentException(format("Cannot interpret '%s' as a '%s'", stringToParse, type));
        }
    }

    private Object coerceMap(String stringToParse, Type keyType, Type valueType) {
        JSONObject jsonObject = new JSONObject(stringToParse);
        int size = jsonObject.length();
        Object map = mapBuilder.newMap(size);
        for (String jsonKey : jsonObject.keySet()) {
            String jsonValue = jsonObject.get(jsonKey).toString();
            Object key = coerceParameter(keyType, jsonKey);
            Object value = coerceParameter(valueType, jsonValue);
            mapBuilder.addElement(map, key, value);
        }
        return map;
    }

    private Object coerceCollection(String stringToParse, Type actualTypeArgument, CollectionBuilder collectionBuilder) {
        JSONArray jsonArray = new JSONArray(stringToParse);
        int size = jsonArray.length();
        Object collection = collectionBuilder.newCollection(actualTypeArgument, size);
        for (int index = 0; index < size; index++) {
            Object element = coerceParameter(actualTypeArgument, jsonArray.get(index).toString());
            collectionBuilder.setElement(collection, index, element);
        }
        return collection;
    }
}
