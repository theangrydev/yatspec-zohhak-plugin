package io.github.theangrydev.yatspeczohhakplugin;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
class MapBuilder {

    static final MapBuilder INSTANCE = new MapBuilder();

    Object newMap(int size) {
        return new HashMap(size);
    }

    void addElement(Object collection, Object key, Object value) {
        Map map = (Map) collection;
        map.put(key, value);
    }
}
