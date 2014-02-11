This allows you to use Javascript in map/reduce views, as opposed to native Java code (the default behavior for Couchbase-Lite).

To "activate" this, you will need to include this library and it's dependencies, and add the following code when you initialize Couchbase-Lite:

```
CBLView.setCompiler(new CBLJavaScriptViewCompiler());
```

See [couchbase-lite-android-liteserv](https://github.com/couchbaselabs/couchbase-lite-android-liteserv) for an example where this is used.
