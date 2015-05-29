package com.couchbase.lite.javascript;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.javascript.scopes.MapGlobalScope;
import com.couchbase.lite.javascript.wrapper.CustomWrapFactory;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.util.Map;

class ViewMapBlockRhino implements Mapper {

    private static WrapFactory wrapFactory = new CustomWrapFactory();
    private Scriptable globalScope;
    private MapGlobalScope mapGlobalScope;
    private Function mapFunction;

    public ViewMapBlockRhino(String src) {

        org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();

        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);
            mapGlobalScope = new MapGlobalScope();
            globalScope = ctx.initStandardObjects(mapGlobalScope, true);
            mapFunction = ctx.compileFunction(globalScope, src, "map", 0, null);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    @Override
    public void map(Map<String, Object> document, Emitter emitter) {

        mapGlobalScope.setEmitter(emitter);

        org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);

            Scriptable localScope = ctx.newObject(globalScope);
            localScope.setPrototype(globalScope);
            localScope.setParentScope(null);

            Object jsDocument = org.mozilla.javascript.Context.javaToJS(document, localScope);

            try {
                mapFunction.call(ctx, localScope, null, new Object[]{jsDocument});
            } catch (org.mozilla.javascript.RhinoException e) {
                // Error in the JavaScript view - CouchDB swallows  the error and tries the next document
                return;
            }
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }
}
