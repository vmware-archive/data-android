/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.data;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

import java.util.Locale;

public class MockHttpResponse implements HttpResponse {
    @Override
    public StatusLine getStatusLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(final StatusLine statusLine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(final ProtocolVersion protocolVersion, final int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(final ProtocolVersion protocolVersion, final int i, final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusCode(final int i) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReasonPhrase(final String s) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpEntity getEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEntity(final HttpEntity httpEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header[] getHeaders(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getFirstHeader(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getLastHeader(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header[] getAllHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(final Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(final String s, final String s2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(final Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(final String s, final String s2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeaders(final Header[] headers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeader(final Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeaders(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HeaderIterator headerIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HeaderIterator headerIterator(final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpParams getParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParams(final HttpParams httpParams) {
        throw new UnsupportedOperationException();
    }
}
