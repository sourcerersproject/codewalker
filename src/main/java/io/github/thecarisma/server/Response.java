package io.github.thecarisma.server;

import io.github.thecarisma.exceptions.ResponseHeaderException;
import io.github.thecarisma.laner.LanerPrintWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adewale Azeez <azeezadewale98@gmail.com>
 */
public class Response {

    private LanerPrintWriter out;
    private Map<String, String[]> headers = new HashMap<>();
    private String rawResponseHead = "" ;
    private String reasonPhrase = "" ;
    private int statusCode = StatusCode.OK;
    private boolean headersNeedsParsing = false;
    public boolean headersSent = false;

    public Response(LanerPrintWriter out) {
        this.out = out;
    }

    public void write(String data) {
        if (!headersSent) {
            sendResponseHead();
        }
        out.write(data);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        if (reasonPhrase.isEmpty()) {
            
        }
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public Map<String, String[]> getHeaders() {
        return headers;
    }

    public void appendHeader(String key, String... values) throws ResponseHeaderException {
        if (headersSent) {
            throw new ResponseHeaderException();
        }
        headers.put(key, values);
        headersNeedsParsing = true;
    }

    public String[] removeHeader(String key) {
        return headers.remove(key);
    }

    public String getHttpVersion() {
        return "HTTP/1.1";
    }

    public void close() {
        if (out.isOpen()) {
            out.close();
        }
    }

    public void sendResponseHead() {
        parseHeaders();
        headersSent = true;
        write(rawResponseHead);
    }

    private void parseHeaders() {
        if (headersNeedsParsing) {
            rawResponseHead = String.format("%s %d %s\r\n", getHttpVersion(), statusCode, getReasonPhrase());
        }
    }

}