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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.lang.String.format;

class ArrayBuilder implements CollectionBuilder {

    @Override
    public Object newCollection(Type elementType, int size) {
        return Array.newInstance(getElementType(elementType), size);
    }

    private Class<?> getElementType(Type elementType) {
        if (elementType instanceof Class) {
            return (Class<?>) elementType;
        } else if (elementType instanceof ParameterizedType) {
            return getElementType(((ParameterizedType) elementType).getRawType());
        } else {
            throw new IllegalStateException(format("Not implemented type %s", elementType));
        }
    }

    @Override
    public void setElement(Object collection, int index, Object element) {
        Array.set(collection, index, element);
    }
}
