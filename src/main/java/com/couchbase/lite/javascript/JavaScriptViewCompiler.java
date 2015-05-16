package com.couchbase.lite.javascript;


import com.couchbase.lite.Mapper;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.ViewCompiler;


public class JavaScriptViewCompiler implements ViewCompiler {
    @Override
    public Mapper compileMap(String source, String language) {
        if (language.equals("javascript")) {
            return new ViewMapBlockRhino(source);
        }
        throw new IllegalArgumentException(language + " is not supported");
    }

    @Override
    public Reducer compileReduce(String source, String language) {
        if (language.equals("javascript")) {
            return new ViewReduceBlockRhino(source);
        }
        throw new IllegalArgumentException(language + " is not supported");
    }
}



