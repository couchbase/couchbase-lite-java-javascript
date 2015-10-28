package com.couchbase.lite.javascript;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.javascript.scopes.MapGlobalScope;
import com.couchbase.lite.javascript.wrapper.CustomWrapFactory;
import com.couchbase.lite.util.Log;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.util.Map;

/**
 * Created by hideki on 10/28/15.
 */
public class JavaScriptReplicationFilter implements ReplicationFilter {
    public static String TAG = "JavaScriptReplicationFilter";

    private static WrapFactory wrapFactory = new CustomWrapFactory();
    private Scriptable globalScope;
    private MapGlobalScope mapGlobalScope;
    private Function mapFunction;


    public JavaScriptReplicationFilter(String src){
        Log.e(TAG, "JavaScriptReplicationFilter(String) src="+src);
        org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();

        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);
            mapGlobalScope = new MapGlobalScope();
            globalScope = ctx.initStandardObjects(mapGlobalScope, true);
            mapFunction = ctx.compileFunction(globalScope, src, "filter", 0, null);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

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
    @Override
    public boolean filter(SavedRevision revision, Map<String, Object> params) {
        Log.e(TAG, "filter(SavedRevision, Map<String, Object>)");
        //mapGlobalScope.setEmitter(emitter);

        org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);

            Scriptable localScope = ctx.newObject(globalScope);
            localScope.setPrototype(globalScope);
            localScope.setParentScope(null);

            Object jsDocument = org.mozilla.javascript.Context.javaToJS(revision.getProperties(), localScope);
            Log.e(TAG, "jsDocument="+jsDocument);

            try {
                Object result = mapFunction.call(ctx, localScope, null, new Object[]{jsDocument});
                Log.e(TAG, "result="+result);
                return ((Boolean)result).booleanValue();
            } catch (org.mozilla.javascript.RhinoException e) {
                // Error in the JavaScript view - CouchDB swallows  the error and tries the next document
                Log.e(TAG, "Error in mapFunction.call()", e);
                return false;
            }
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }
}
