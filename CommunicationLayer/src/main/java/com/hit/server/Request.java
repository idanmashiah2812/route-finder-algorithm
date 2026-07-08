package com.hit.server;

import java.util.Map;

public class Request<T> {

    private Map<String, String> headers;
    private T body;

    public Request() {
    }

    public Request(Map<String, String> headers, T body) {
        this.headers = headers;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
