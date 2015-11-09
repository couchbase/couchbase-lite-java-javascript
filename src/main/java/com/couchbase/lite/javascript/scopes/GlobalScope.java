package com.couchbase.lite.javascript.scopes;

import com.couchbase.lite.util.Log;

import org.mozilla.javascript.ScriptableObject;

public class GlobalScope extends ScriptableObject {
    public GlobalScope() {
        super();
        String[] names = {"log"};
        this.defineFunctionProperties(names, GlobalScope.class, ScriptableObject.DONTENUM);
    }

    public static void log(Object msg) {
        // right tag?
        // TODO maybe more sophisticated string conversion
        Log.d(Log.TAG_VIEW, msg.toString());
    }

    @Override
    public String getClassName() {
        return "global";
    }
}
