package com.couchbase.lite.javascript.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmitPair {
    Object key;
    Object value;

    private ObjectMapper mapper = new ObjectMapper();

    public EmitPair(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof EmitPair)) return false;
        EmitPair that = (EmitPair) aThat;
        if (this.key == that.key && this.value == that.value) return true;

        String jsonKey = toJson(this.key);
        String jsonValue = toJson(this.value);
        String thatJsonKey = toJson(that.key);
        String thatJsonValue = toJson(that.value);

        System.out.print(jsonKey + " = " + thatJsonKey);
        System.out.print(jsonValue + " = " + thatJsonValue);
        if (jsonKey.equals(thatJsonKey) && jsonValue.equals(thatJsonValue)) return true;

        return false;
    }

    private String toJson(Object obj) {
        String json = null;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}