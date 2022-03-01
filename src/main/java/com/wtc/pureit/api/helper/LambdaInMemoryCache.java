package com.wtc.pureit.api.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

@Slf4j
public class LambdaInMemoryCache {

    public final LinkedHashMap<Object, Object> inMemoryMap = new LinkedHashMap<>();

    public void save(Object key, Object value) {
        inMemoryMap.put(key, value);
    }

    public Object retrieve(Object key) {
        synchronized (inMemoryMap) {
            log.info("Retrieving: {} and it's value is {}", key, inMemoryMap.get(key));

            return inMemoryMap.get(key);
        }
    }

    public void clearInMemory() {
        inMemoryMap.clear();
    }

}
