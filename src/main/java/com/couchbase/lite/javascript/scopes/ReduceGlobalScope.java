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

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Status;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * Created by stefan on 10.05.15.
 */
public class ReduceGlobalScope extends GlobalScope {

    public ReduceGlobalScope() {
        super();
        String[] names = {"sum"};
        this.defineFunctionProperties(names, ReduceGlobalScope.class, ScriptableObject.DONTENUM);
    }

    // rhino numbers are double, so we return double
    public static double sum(Object o) throws CouchbaseLiteException {
        if (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        if (o instanceof NativeArray) {
            o = ((NativeArray) o).toArray();
        }

        if (o.getClass().isArray()) {
            Object[] arr = (Object[]) o;
            double sum = 0;
            for (int i = 0; i < arr.length; i++) {
                sum = sum + Double.valueOf(String.valueOf(arr[i]));
            }
            return sum;
        }

        throw new CouchbaseLiteException("don't know how to sum" + o.toString(), new Status());

    }

}
