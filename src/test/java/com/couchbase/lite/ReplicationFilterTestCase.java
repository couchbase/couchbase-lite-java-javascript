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
    public void testInvalidLanguage() {
        try {
            ReplicationFilter replicationFilter = replicationFilterCompiler.compileFilterFunction("function(doc, req) {if(doc.type && doc.type == 'order' && req.abc == 1) {return true;}else{return false}}", "C++");
            fail("IllegalArgumentException should be thrown");
        }
        catch (IllegalArgumentException e){
        }
    }
}
