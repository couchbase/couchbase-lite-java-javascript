package com.couchbase.lite.testapp.javascript.tests;

import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.testapp.javascript.tests.helper.EmitPair;
import com.couchbase.lite.testapp.javascript.tests.helper.MockEmmiter;

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
    private MockEmmiter emiter;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        viewCompiler = new JavaScriptViewCompiler();
        emiter = new MockEmmiter();
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
}
