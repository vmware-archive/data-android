/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/* package */ class StreamUtils {

    public static String getStringAndClose(final InputStream inputStream) throws IOException {
        try {
            final InputStreamReader streamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(streamReader);
            final StringBuilder stringBuilder = new StringBuilder();
            return getString(bufferedReader, stringBuilder);
        } finally {
            inputStream.close();
        }
    }

    private static String getString(final BufferedReader reader, final StringBuilder builder) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }
}
