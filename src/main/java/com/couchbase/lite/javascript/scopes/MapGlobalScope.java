package com.couchbase.lite.javascript.scopes;

import com.couchbase.lite.Emitter;

import org.mozilla.javascript.ScriptableObject;

public class MapGlobalScope extends GlobalScope {
    private Emitter emitter;

    public MapGlobalScope() {
        super();
        String[] names = {"emit"};
        this.defineFunctionProperties(names, MapGlobalScope.class, ScriptableObject.DONTENUM);
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    // emit might fail when called in a method of a JS Object
    // I don't think that is an issue - who will create a Object in a view?
    // solution would be to make it static and get the emitter from the "thisObj"
    // see Rhino FunctionObject documentation (keyword varargs, "second form")

    public void emit(Object key, Object value) {
        emitter.emit(key, value);
    }
}
