package com.jackdaw.chatwithnpc;

import java.util.concurrent.ConcurrentHashMap;

public class BaseManager<K, V> {
    protected final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public boolean isRegistered(K key) {
        return map.containsKey(key);
    }

    public void register(K key, V value) {
        if (!isRegistered(key)) map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    /**
     * This will be called before remove an Object or clear the Registry.
     * @param key key
     * @return If false, the remove process will be blocked.
     */
    protected boolean discard(K key) {
        return true;
    }

    public void remove(K key) {
        if (isRegistered(key) && discard(key)) {
            map.remove(key);
        }
    }

    protected void clear() {
        map.forEach((key, value) -> remove(key));
    }
}
