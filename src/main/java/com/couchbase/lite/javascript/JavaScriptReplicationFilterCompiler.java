package com.couchbase.lite.javascript;

import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.ReplicationFilterCompiler;
import com.couchbase.lite.util.Log;

/**
 * Created by hideki on 10/28/15.
 */
public class JavaScriptReplicationFilterCompiler implements ReplicationFilterCompiler {
    public static String TAG = "JSReplicationFilterCompiler";
    @Override
    public ReplicationFilter compileFilterFunction(String source, String language) {
        Log.e(TAG, "compileFilterFunction(String, String)");
        return new JavaScriptReplicationFilter(source);
    }
}
