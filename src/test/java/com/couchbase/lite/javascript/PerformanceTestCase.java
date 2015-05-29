package com.couchbase.lite.javascript;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.javascript.helper.MockEmiter;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stefan on 16.05.15.
 */

/*
  NOT significant, Android devices behave different, GC can ruin everything.
 */
public class PerformanceTestCase extends TestCase {

    private JavaScriptViewCompiler viewCompiler;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        viewCompiler = new JavaScriptViewCompiler();
    }

    public void test200SimpleJavaScriptView() {
        MockEmiter jsEmiter = new MockEmiter();

        Map doc = new HashMap();
        doc.put("_id", "foo");

        Mapper jsMap = viewCompiler.compileMap("function(doc){emit(doc._id,null);}", "javascript");

        for (int i = 0; i < 20000; i++) {
            jsMap.map(doc, jsEmiter);
        }
        assertEquals(20000, jsEmiter.count());
    }

    public void test200SimpleNativeView() {
        MockEmiter nativeEmiter = new MockEmiter();

        Map doc = new HashMap();
        doc.put("_id", "foo");

        Mapper nativeMap = new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                emitter.emit(document.get("_id"), null);
            }
        };

        for (int i = 0; i < 20000; i++) {
            nativeMap.map(doc, nativeEmiter);
        }
        assertEquals(20000, nativeEmiter.count());
    }
}
