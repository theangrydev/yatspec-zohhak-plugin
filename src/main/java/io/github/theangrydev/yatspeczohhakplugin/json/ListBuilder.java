package io.github.theangrydev.yatspeczohhakplugin.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
class ListBuilder implements CollectionBuilder {

    static final ListBuilder INSTANCE = new ListBuilder();

    @Override
    public Object newCollection(Type elementType, int size) {
        return new ArrayList(size);
    }

    @Override
    public void setElement(Object collection, int index, Object element) {
        List list = (List) collection;
        list.add(element);
    }
}
