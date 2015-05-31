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
