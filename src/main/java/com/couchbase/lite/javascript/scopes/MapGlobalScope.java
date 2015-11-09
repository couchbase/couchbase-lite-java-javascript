/**
 * Copyright (c) 2015 Couchbase, Inc All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
