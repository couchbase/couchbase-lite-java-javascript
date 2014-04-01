package com.couchbase.lite.javascript;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.elasticsearch.script.javascript.support.NativeList;
import org.elasticsearch.script.javascript.support.NativeMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.ViewCompiler;
import com.couchbase.lite.util.Log;

public class JavaScriptViewCompiler implements ViewCompiler {

	@Override
	public Mapper compileMap(String source, String language) {
        if (language.equalsIgnoreCase("javascript")) {
            return new ViewMapBlockRhino(source);
        }

        throw new IllegalArgumentException(language + " is not supported");
	}

	@Override
	public Reducer compileReduce(String source, String language) {
        if (language.equalsIgnoreCase("javascript")) {
            return new ViewReduceBlockRhino(source);
        }

        throw new IllegalArgumentException(language + " is not supported");
	}
}

/**
 * Wrap Factory for Rhino Script Engine
 */
class CustomWrapFactory extends WrapFactory {

    public CustomWrapFactory() {
        setJavaPrimitiveWrap(false); // RingoJS does that..., claims its annoying...
    }

	@Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
        if (javaObject instanceof Map) {
            return new NativeMap(scope, (Map) javaObject);
        } else if (javaObject instanceof List) {
            return new NativeList(scope, (List<Object>)javaObject);
        }

        return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
    }
}

// REFACT: Extract superview for both the map and reduce blocks as they do pretty much the same thing

class ViewMapBlockRhino implements Mapper {

    private final String mapSrc;
    private final Scriptable globalScope;
    private final Script placeHolder;

    private static final WrapFactory wrapFactory = new CustomWrapFactory();
    private static final ObjectMapper mapper = new ObjectMapper();

    public ViewMapBlockRhino(String src) {
        mapSrc = src;

        mapper.getJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        final Context ctx = Context.enter();

        try {
            // Android dex won't allow us to create our own classes
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);
            globalScope = ctx.initStandardObjects(null, true);

            // create a place to hold results
            final String resultArray = "var map_results = [];";
            placeHolder = ctx.compileString(resultArray, "placeHolder", 1, null);

            try {
                //register the emit function
                final String emitFunction = "var emit = function(key, value) { map_results.push([key, value]); };";
                ctx.evaluateString(globalScope, emitFunction, "emit", 1, null);

                // register the map function
                final String map = "var map = " + mapSrc + ";";
                ctx.evaluateString(globalScope, map, "map", 1, null);
            } catch(org.mozilla.javascript.EvaluatorException e) {
                // Error in the JavaScript view - CouchDB swallows  the error and tries the next document
                Log.e(Database.TAG, "Javascript syntax error in view:\n" + src, e);
                return;
            }
        } finally {
            Context.exit();
        }
    }

	@Override
    public void map(Map<String, Object> document, Emitter emitter) {
        Context ctx = Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);

            // empty out the array that may have been filled by a previous call of this method
            ctx.executeScriptWithContinuations(placeHolder, globalScope);
            
            // Need to stringify the json tree, as the ContextWrapper is unable
            // to correctly convert nested json to their js representation.
            // More specifically, if a dictionary is included that contains an array as a value 
            // that array will not be wrapped correctly but you'll get the plain 
            // java.util.ArrayList instead - and then an error.
            try {

//                mapper.getSerializationConfig().with(SerializationConfig.Feature)
                // One thing that CouchDB does is replace these whitespace/newlines values with null-bytes
                final String json = mapper.writeValueAsString(document).replace("\\u2028", "\0");

//                while ((t = json.indexOf('\u2028')) > 0) {
//                    json = json.substring(0, t-1) + "\\n" + json.substring(t+1, json.length());
//                }

                final String mapInvocation = "map(" + json + ");";

                ctx.evaluateString(globalScope, mapInvocation, "map invocation", 1, null);
			} catch (org.mozilla.javascript.RhinoException e) {
                // Error in the JavaScript view - CouchDB swallows  the error and tries the next document
                Log.e(Database.TAG, "Error in javascript view:\n" + mapSrc + "\n with document:\n" + document, e);
                return;
            } catch (IOException e) {
				// Can thrown different subclasses of IOException- but we really do not care,
				// as this document was unserialized from JSON, so Jackson should be able to serialize it. 
				Log.e(Database.TAG, "Error reserializing json from the db: " + document, e);
				return;
			}

            //now pull values out of the place holder and emit them
            final NativeArray mapResults = (NativeArray) globalScope.get("map_results", globalScope);

            final int resultSize = (int) mapResults.getLength();

            for (int i=0; i< resultSize; i++) {
                final NativeArray mapResultItem = (NativeArray) mapResults.get(i);

                if (mapResultItem != null && mapResultItem.getLength() == 2) {
                    Object key = mapResultItem.get(0);
                    Object value = mapResultItem.get(1);
                    emitter.emit(key, value);
                } else {
                    Log.e(Database.TAG, "Expected 2 element array with key and value");
                }
            }
        } finally {
            Context.exit();
        }
    }
}

class ViewReduceBlockRhino implements Reducer {

    private final Scriptable globalScope;

    private static final WrapFactory wrapFactory = new CustomWrapFactory();

    public ViewReduceBlockRhino(String src) {
        Context ctx = Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);

            globalScope = ctx.initStandardObjects(null, true);

            // register the reduce function
            final String reduceSrc = "var reduce = " + src + ";";
            ctx.evaluateString(globalScope, reduceSrc, "reduce", 1, null);
        } finally {
            Context.exit();
        }
    }

	@Override
    public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
        Context ctx = Context.enter();
        try {
            ctx.setOptimizationLevel(-1);
            ctx.setWrapFactory(wrapFactory);

            // find the reduce function and execute it
            Function reduceFun = (Function) globalScope.get("reduce", globalScope);
            Object[] functionArgs = { keys, values, rereduce };

            return reduceFun.call(ctx, globalScope, globalScope, functionArgs);
        } finally {
            Context.exit();
        }
    }
}
