package com.couchbase.lite.javascript.helper;

import com.couchbase.lite.Emitter;

import java.util.ArrayList;
import java.util.List;

public class MockEmiter implements Emitter {

    private List<EmitPair> emited = new ArrayList<EmitPair>();

    @Override
    public void emit(Object key, Object value) {
        emited.add(new EmitPair(key, value));
    }

    public int count() {
        return emited.size();
    }

    public EmitPair get(int i) {
        return emited.get(i);
    }
}