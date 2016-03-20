package io.github.theangrydev.yatspeczohhakplugin;

import java.lang.reflect.Type;

interface CollectionBuilder {
    Object newCollection(Type elementType, int size);
    void setElement(Object collection, int index, Object element);
}
