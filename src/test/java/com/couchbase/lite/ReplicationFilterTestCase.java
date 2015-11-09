package com.couchbase.lite;

import com.couchbase.lite.internal.RevisionInternal;
import com.couchbase.lite.javascript.JavaScriptReplicationFilterCompiler;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hideki on 11/9/15.
 */
public class ReplicationFilterTestCase extends TestCase {
    private JavaScriptReplicationFilterCompiler replicationFilterCompiler;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        replicationFilterCompiler = new JavaScriptReplicationFilterCompiler();
    }

    public void testSimpleFilterTrue() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("_id", "foo");
        props.put("_rev", "1-1111");
        props.put("_deleted", false);
        props.put("type", "order");
        props.put("SeatNumber", 10);
        RevisionInternal revisionInternal = new RevisionInternal(props);
        SavedRevision savedRevision = new SavedRevision((Document)null, revisionInternal);
        ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order') {return true;}else{return false}}", "javascript");
        assertTrue(replicationFilter.filter(savedRevision, null));
    }

    public void testSimpleFilterFalse() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("_id", "foo");
        props.put("_rev", "1-1111");
        props.put("_deleted", false);
        props.put("type", "user");
        props.put("Name", "tom");
        RevisionInternal revisionInternal = new RevisionInternal(props);
        SavedRevision savedRevision = new SavedRevision((Document)null, revisionInternal);
        ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order') {return true;}else{return false}}", "javascript");
        assertFalse(replicationFilter.filter(savedRevision, null));
    }


    public void testSimpleFilterWithReqTrue() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("_id", "foo");
        props.put("_rev", "1-1111");
        props.put("_deleted", false);
        props.put("type", "order");
        props.put("SeatNumber", 10);
        RevisionInternal revisionInternal = new RevisionInternal(props);
        SavedRevision savedRevision = new SavedRevision((Document)null, revisionInternal);
        ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order' && req.abc == 1) {return true;}else{return false}}", "javascript");
        Map req = new HashMap();
        req.put("abc", 1);
        assertTrue(replicationFilter.filter(savedRevision, req));
    }

    public void testSimpleFilterWithReqFalse() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("_id", "foo");
        props.put("_rev", "1-1111");
        props.put("_deleted", false);
        props.put("type", "user");
        props.put("Name", "tom");
        RevisionInternal revisionInternal = new RevisionInternal(props);
        SavedRevision savedRevision = new SavedRevision((Document)null, revisionInternal);
        ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order' && req.abc == 1) {return true;}else{return false}}", "javascript");
        Map req = new HashMap();
        req.put("abc", 2);
        assertFalse(replicationFilter.filter(savedRevision, req));
    }
    public void testSimpleFilterWithReqFalse2() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("_id", "foo");
        props.put("_rev", "1-1111");
        props.put("_deleted", false);
        props.put("type", "user");
        props.put("Name", "tom");
        RevisionInternal revisionInternal = new RevisionInternal(props);
        SavedRevision savedRevision = new SavedRevision((Document)null, revisionInternal);
        ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order' && req.abc == 1) {return true;}else{return false}}", "javascript");
        assertFalse(replicationFilter.filter(savedRevision, null));
    }
}
