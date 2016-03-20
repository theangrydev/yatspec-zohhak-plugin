package io.github.theangrydev.yatspeczohhakplugin;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
class SetBuilder implements CollectionBuilder {

    static final SetBuilder INSTANCE = new SetBuilder();

    @Override
    public Object newCollection(Type elementType, int size) {
        return new HashSet(size);
    }

    @Override
    public void setElement(Object collection, int index, Object element) {
        Set set = (Set) collection;
        set.add(element);
    }
}
