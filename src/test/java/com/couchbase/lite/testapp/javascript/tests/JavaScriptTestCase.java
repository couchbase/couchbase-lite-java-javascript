package com.couchbase.lite.testapp.javascript.tests;

import com.couchbase.lite.Mapper;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.testapp.javascript.tests.helper.EmitPair;
import com.couchbase.lite.testapp.javascript.tests.helper.MockEmiter;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stefan on 16.05.15.
 */
public class JavaScriptTestCase extends TestCase {

    private JavaScriptViewCompiler viewCompiler;
    private MockEmiter emiter;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        viewCompiler = new JavaScriptViewCompiler();
        emiter = new MockEmiter();
    }

    public void testSimpleJavaScriptView() {
        Map doc = new HashMap();
        doc.put("_id", "foo");

        Mapper map = viewCompiler.compileMap("function(doc){emit(doc._id,null);}", "javascript");

        map.map(doc, emiter);

        assertEquals(1, emiter.count());
        assertEquals(new EmitPair("foo", null), emiter.get(0));
    }

    public void testReduceSum() {
        List<Object> keys = new ArrayList<Object>(5);
        List<Object> values = new ArrayList<Object>(5);

        values.add(0, 1);
        values.add(1, 2);
        values.add(2, 3);
        values.add(3, 4);
        values.add(4, 5);

        Reducer reduce = viewCompiler.compileReduce("function(keys, values, rereduce){return sum(values)}", "javascript");

        Object result = reduce.reduce(keys, values, false);

        assertEquals(new Double(15), result);
    }

    public void testReduceNativeSum() {
        List<Object> keys = new ArrayList<Object>(5);
        List<Object> values = new ArrayList<Object>(5);

        values.add(0, 1);
        values.add(1, 2);
        values.add(2, 3);
        values.add(3, 4);
        values.add(4, 5);

        Reducer reduce = viewCompiler.compileReduce("_sum", "javascript");

        Object result = reduce.reduce(keys, values, false);

        assertEquals(new Double(15), result);
    }

    public void testReduceCount() {
        List<Object> keys = new ArrayList<Object>(5);
        List<Object> values = new ArrayList<Object>(5);

        values.add(0, 1);
        values.add(1, 2);
        values.add(2, 3);
        values.add(3, 4);
        values.add(4, 5);

        Reducer reduce = viewCompiler.compileReduce("function(keys, values, rereduce){  if (rereduce) {  return sum(values); } else {   return values.length; }}", "javascript");

        Object result = reduce.reduce(keys, values, false);
        assertEquals(new Double(5), result);

        // re-reduce
        result = reduce.reduce(keys, values, true);
        assertEquals(new Double(15), result);
    }

    public void testReduceKeysAndValuesAreArrays() {

        List<Object> keys = new ArrayList<Object>(0);
        List<Object> values = new ArrayList<Object>(0);

        Reducer reduce = viewCompiler.compileReduce("function(keys, values, rereduce){ return (Array.isArray(keys) && Array.isArray(values)) }", "javascript");

        Object result = reduce.reduce(keys, values, false);
        assertEquals(true, result);
    }

    public void testReduceNativeCount() {
        List<Object> keys = new ArrayList<Object>(5);
        List<Object> values = new ArrayList<Object>(5);

        values.add(0, 1);
        values.add(1, 2);
        values.add(2, 3);
        values.add(3, 4);
        values.add(4, 5);

        Reducer reduce = viewCompiler.compileReduce("_count", "javascript");

        Object result = reduce.reduce(keys, values, false);
        assertEquals(new Double(5), result);

        // re-reduce
        result = reduce.reduce(keys, values, true);
        assertEquals(new Double(15), result);
    }

    public void testScopeSeparation() {
        Map doc = new HashMap();
        doc.put("_id", "foo");

        // try to put a var "x" into global scope
        Mapper map = viewCompiler.compileMap("function(doc){var x = x || 0; emit(x++,null);}", "javascript");

        map.map(doc, emiter);
        map.map(doc, emiter);
        map.map(doc, emiter);

        assertEquals(3, emiter.count());
        assertEquals(new EmitPair(0.0, null), emiter.get(0));
        assertEquals(new EmitPair(0.0, null), emiter.get(1));
        assertEquals(new EmitPair(0.0, null), emiter.get(2));
    }

    public void testJavaScriptDesignDocument() {
        Map<String, Object> doc1 = new HashMap<String, Object>();
        doc1.put("_id", "doc1");
        doc1.put("message", "hello");
        Map<String, Object> doc2 = new HashMap<String, Object>();
        doc2.put("_id", "doc2");
        doc2.put("message", "guten tag");
        Map<String, Object> doc3 = new HashMap<String, Object>();
        doc3.put("_id", "doc3");
        doc3.put("message", "bonjour");

        Mapper map = viewCompiler.compileMap("function(doc) { if(doc.message) { emit(doc.message, 1); } }", "javascript");

        map.map(doc1, emiter);
        map.map(doc2, emiter);
        map.map(doc3, emiter);

        assertEquals(3, emiter.count());
        assertEquals(new EmitPair("hello", 1.0), emiter.get(0));
        assertEquals(new EmitPair("guten tag", 1.0), emiter.get(1));
        assertEquals(new EmitPair("bonjour", 1.0), emiter.get(2));
    }

    public void testRealJavaScriptDesignDocument() {
        Map<String, Object> doc1 = new HashMap<String, Object>();
        doc1.put("_id", "doc1");
        List<String> cat1 = new ArrayList<String>();
        cat1.add("apple");
        cat1.add("bannana");
        doc1.put("categories", cat1);

        Map<String, Object> doc2 = new HashMap<String, Object>();
        doc2.put("_id", "doc2");
        List<String> cat2 = new ArrayList<String>();
        cat2.add("clock");
        cat2.add("dill");
        doc2.put("categories", cat2);

        Map<String, Object> doc3 = new HashMap<String, Object>();
        doc3.put("_id", "doc3");
        List<String> cat3 = new ArrayList();
        cat3.add("elephant");
        cat3.add("fun");
        cat3.add("apple");
        doc3.put("categories", cat3);

        Mapper map = viewCompiler.compileMap("function(doc) { if (doc.categories) { for (i in doc.categories) { emit(doc.categories[i], 1); } } }", "javascript");

        map.map(doc1, emiter);
        map.map(doc2, emiter);
        map.map(doc3, emiter);

        assertEquals(7, emiter.count());
        assertEquals(new EmitPair("apple", 1.0), emiter.get(0));
        assertEquals(new EmitPair("bannana", 1.0), emiter.get(1));
        assertEquals(new EmitPair("clock", 1.0), emiter.get(2));
        assertEquals(new EmitPair("dill", 1.0), emiter.get(3));
        assertEquals(new EmitPair("elephant", 1.0), emiter.get(4));
        assertEquals(new EmitPair("fun", 1.0), emiter.get(5));
        assertEquals(new EmitPair("apple", 1.0), emiter.get(6));
    }

    public void testJavaScriptDesignDocumentThatDealsWithArrays() {
        Map<String, Object> doc1 = new HashMap<String, Object>();
        doc1.put("_id", "doc1");
        List<String> producers1 = new ArrayList<String>();
        producers1.add("abc");
        doc1.put("producers", producers1);
        doc1.put("collection", "product");

        Mapper map = viewCompiler.compileMap("function(doc) { if ('product' === doc.collection && doc.producers) { doc.producers.forEach(function(each) { emit(each, doc); }); } }", "javascript");

        map.map(doc1, emiter);

        assertEquals(1, emiter.count());
        assertEquals(new EmitPair("abc", doc1), emiter.get(0));
    }

    public void testShouldLeaveOutDocumentsWhenMapBlockThrowsAnException() {
        Map<String, Object> doc1 = new HashMap<String, Object>();
        doc1.put("_id", "good");
        Map<String, Object> doc2 = new HashMap<String, Object>();
        doc2.put("_id", "bad");

        Mapper map = viewCompiler.compileMap("function(doc) {  if (doc._id === 'bad') throw new Error('gotcha!'); emit(1, doc); }", "javascript");

        map.map(doc1, emiter);
        map.map(doc2, emiter);

        assertEquals(1, emiter.count());
        assertEquals(new EmitPair(1.0, doc1), emiter.get(0));
    }

    public void testShouldReturnEmptyViewIfJavaScriptIsErranous() {
        Map<String, Object> doc1 = new HashMap<String, Object>();
        doc1.put("_id", "good");

        Mapper map = viewCompiler.compileMap("function(doc) { }", "javascript");

        map.map(doc1, emiter);

        assertEquals(0, emiter.count());
    }
}
