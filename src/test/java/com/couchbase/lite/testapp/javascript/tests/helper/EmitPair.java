package com.couchbase.lite.testapp.javascript.tests.helper;

import com.google.gson.Gson;

public class EmitPair {
    Object key;
    Object value;

    private static Gson gson = new Gson();

    public EmitPair(Object key, Object value) {
        this.key = key;
        this.value = value;
    }


    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof EmitPair)) return false;
        EmitPair that = (EmitPair) aThat;
        if (this.key == that.key && this.value == that.value) return true;

        String jsonKey = gson.toJson(this.key);
        String jsonValue = gson.toJson(this.value);
        String thatJsonKey = gson.toJson(that.key);
        String thatJsonValue = gson.toJson(that.value);


        System.out.print(jsonKey +" = "+ thatJsonKey);
        System.out.print(jsonValue +" = "+ thatJsonValue);
        if(jsonKey.equals(thatJsonKey) && jsonValue.equals(thatJsonValue)) return true;


        return false;
    }

}