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
import java.lang.reflect.Type;

class ArrayBuilder implements CollectionBuilder {

    private final Object array;
    private int index;

    ArrayBuilder(Type elementType, int size) {
        this.array = Array.newInstance((Class<?>) elementType, size);
    }

    @Override
    public void add(Object element) {
        Array.set(array, index++, element);
    }

    @Override
    public Object build() {
        return array;
    }
}
