//
// Copyright (c) 2016 Couchbase, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing permissions
// and limitations under the License.
//
package com.couchbase.lite.javascript;

import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.javascript.scopes.GlobalScope;
import com.couchbase.lite.javascript.wrapper.CustomWrapFactory;
import com.couchbase.lite.util.Log;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.util.Map;

/**
 * Created by hideki on 10/28/15.
 */
public class ReplicationFilterBlockRhino implements ReplicationFilter {
    public static String TAG = "JavaScriptEngine";

    private static WrapFactory wrapFactory = new CustomWrapFactory();
    private Scriptable scope;
    private GlobalScope globalScope;
    private Function filterFunction;

    // NOTE: Scope is sharable with multiple threads, it seems `Function` is not.
    //       Compiling javascript codes for every request makes slow.
    //       It is reason current code base re-use compiled Function.
    //       Instead of compiling for every request, use `synchronized` to protect Function
    //       https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino/Scopes_and_Contexts
    private final Object lockFunction = new Object();

    public ReplicationFilterBlockRhino(String src) {
        org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);
            globalScope = new GlobalScope();
            scope = ctx.initStandardObjects(globalScope, true);
            filterFunction = ctx.compileFunction(scope, src, "filter", 0, null);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    @Override
    public boolean filter(SavedRevision revision, Map<String, Object> params) {
        synchronized (lockFunction) {
            org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
            try {
                ctx.setOptimizationLevel(-1);
                ctx.setWrapFactory(wrapFactory);
                Scriptable localScope = ctx.newObject(scope);
                localScope.setPrototype(scope);
                localScope.setParentScope(null);
                Object jsDocument = org.mozilla.javascript.Context.javaToJS(revision.getProperties(), localScope);
                Object jsParams = org.mozilla.javascript.Context.javaToJS(params, localScope);

                try {
                    Object result = filterFunction.call(ctx, localScope, null, new Object[]{jsDocument, jsParams});
                    return ((Boolean) result).booleanValue();
                } catch (org.mozilla.javascript.RhinoException e) {
                    Log.e(TAG, "Error in filterFunction.call()", e);
                    return false;
                }
            } finally {
                org.mozilla.javascript.Context.exit();
            }
        }
    }
}
