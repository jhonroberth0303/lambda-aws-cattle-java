package com.cattle.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EntityPatch(Map<String, Object> set, List<String> remove) {

    public static EntityPatch of() {
        return new EntityPatch(new LinkedHashMap<>(), new ArrayList<>());
    }

    public EntityPatch set(String k, Object v) {
        set.put(k, v);
        return this;
    }

    public EntityPatch remove(String k) {
        remove.add(k);
        return this;
    }

    public boolean isEmpty() {
        return set.isEmpty() && remove.isEmpty();
    }
}

