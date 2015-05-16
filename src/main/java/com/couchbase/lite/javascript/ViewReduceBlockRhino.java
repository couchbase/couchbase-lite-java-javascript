package com.couchbase.lite.javascript;


import com.couchbase.lite.Reducer;
import com.couchbase.lite.javascript.scopes.ReduceGlobalScope;
import com.couchbase.lite.javascript.wrapper.CustomWrapFactory;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

import java.util.List;


class ViewReduceBlockRhino implements Reducer {

    private static WrapFactory wrapFactory = new CustomWrapFactory();
    private final ReduceGlobalScope reduceGlobalScope;
    private final ScriptableObject globalScope;
    private final Function reduceFunction;
    private final NativReduceFunctions nativeReduce;

    public ViewReduceBlockRhino(String src) {


        nativeReduce = NativReduceFunctions.fromKey(src);

        if (nativeReduce == NativReduceFunctions.DEFAULT) {
            org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();

            try {
                ctx.setOptimizationLevel(-1);
                ctx.setWrapFactory(wrapFactory);
                reduceGlobalScope = new ReduceGlobalScope();
                globalScope = ctx.initStandardObjects(reduceGlobalScope, true);
                reduceFunction = ctx.compileFunction(globalScope, src, "reduce", 0, null);
            } finally {
                org.mozilla.javascript.Context.exit();
            }
        } else {
            // not needed if no JS is executed
            reduceGlobalScope = null;
            globalScope = null;
            reduceFunction = null;
        }
    }


    @Override
    public Object reduce(List<Object> keys, List<Object> values, boolean reReduce) {

        switch (nativeReduce) {
            case SUM:
                return nativeSum(keys, values, reReduce);
            case COUNT:
                return nativeCount(keys, values, reReduce);
            case DEFAULT:
            default:
                org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
                try {
                    ctx.setOptimizationLevel(-1);
                    ctx.setWrapFactory(wrapFactory);

                    Scriptable localScope = ctx.newObject(globalScope);
                    localScope.setPrototype(globalScope);
                    localScope.setParentScope(null);

                    Object[] args = new Object[3];

                    args[0] = org.mozilla.javascript.Context.javaToJS(keys, localScope);
                    args[1] = org.mozilla.javascript.Context.javaToJS(values, localScope);
                    args[2] = org.mozilla.javascript.Context.javaToJS(reReduce, localScope);

                    return reduceFunction.call(ctx, localScope, null, args);

                } catch (org.mozilla.javascript.RhinoException e) {
                    // TODO check couchdb behaviour on error in reduce function
                    return null;
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
        }
    }

    // rhino numbers are double, so we return double not int
    private static double nativeSum(List<Object> keys, List<Object> values, boolean reReduce) {

        double sum = 0;
        int length = values.size();
        for (int i = 0; i < length; i++) {
            sum = sum + new Double(String.valueOf(values.get(i)));
        }
        return sum;
    }

    // rhino numbers are double, so we return double not int
    private static double nativeCount(List<Object> keys, List<Object> values, boolean reReduce) {
        if (reReduce) {
            return nativeSum(keys, values, reReduce);
        } else {
            return new Double(values.size());
        }
    }

    enum NativReduceFunctions {
        // TODO add _stats
        COUNT("_count"), SUM("_sum"), DEFAULT("default");
        private String mKey;

        private NativReduceFunctions(String key) {
            mKey = key;
        }

        public static NativReduceFunctions fromKey(String key) {
            if (key != null) {
                for (NativReduceFunctions type : values()) {
                    if (key.equalsIgnoreCase(type.mKey)) {
                        return type;
                    }
                }
            }
            return DEFAULT;
        }

        public String getKey() {
            return mKey;
        }

        @Override
        public String toString() {
            return mKey;
        }
    }
}