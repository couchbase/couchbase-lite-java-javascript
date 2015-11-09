package com.couchbase.lite.javascript;

import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.ReplicationFilterCompiler;

/**
 * Created by hideki on 10/28/15.
 */
public class JavaScriptReplicationFilterCompiler implements ReplicationFilterCompiler {
    @Override
    public ReplicationFilter compileFilterFunction(String source, String language) {
        return new ReplicationFilterBlockRhino(source);
    }
}
