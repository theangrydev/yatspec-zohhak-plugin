package io.github.theangrydev.yatspeczohhakplugin;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.lang.String.format;

class ArrayBuilder implements CollectionBuilder {

    static final ArrayBuilder INSTANCE = new ArrayBuilder();

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
