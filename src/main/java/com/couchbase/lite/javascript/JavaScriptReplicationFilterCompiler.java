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

package com.couchbase.lite.javascript;

import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.ReplicationFilterCompiler;

/**
 * Created by hideki on 10/28/15.
 */
public class JavaScriptReplicationFilterCompiler implements ReplicationFilterCompiler {
    @Override
    public ReplicationFilter compileFilterFunction(String source, String language) {
        if (language != null && language.equalsIgnoreCase("javascript")) {
            return new ReplicationFilterBlockRhino(source);
        }
        throw new IllegalArgumentException(language + " is not supported");
    }
}
