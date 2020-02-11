package io.github.thecarisma.server;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MultipartStream {

    public final String boundary ;
    private InputStream inputStream;
    private String paddingLeft = "--";
    private boolean hasNext_ = false;
    private boolean streamDone = false;

    public MultipartStream(String rawBody, String boundary) {
        if (boundary.isEmpty()) {
            hasNext_ = false;
            streamDone = true;
        }
        inputStream = new ByteArrayInputStream(rawBody.getBytes(StandardCharsets.UTF_8));
        this.boundary = boundary.trim().replaceAll("boundary=", paddingLeft);
    }

    public MultipartStream(InputStream inputStream, String boundary) {
        if (boundary.isEmpty()) {
            hasNext_ = false;
            streamDone = true;
        }
        this.inputStream = inputStream;
        this.boundary = boundary.trim().replaceAll("boundary=", paddingLeft);
    }

    public MultipartStream(InputStream inputStream, String boundary, String paddingLeft) {
        if (boundary.isEmpty()) {
            hasNext_ = false;
            streamDone = true;
        }
        this.inputStream = inputStream;
        this.boundary = boundary.trim().replaceAll("boundary=", paddingLeft);
    }

    public boolean hasNext() throws IOException {
        if (!hasNext_ && !streamDone) {
            StringBuilder tmpBoundary = new StringBuilder();
            while (inputStream.ready()) {
                char c = (char) inputStream.read();
                if (c == '\r') {
                    int x = inputStream.read(); //read \r\n
                    break;
                }
                tmpBoundary.append(c);
            }
            hasNext_ = tmpBoundary.toString().equals(boundary);
        }
        return hasNext_;
    }

    public MultipartData next() throws IOException {
        MultipartData multipartData = new MultipartData();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream currentLineOut = new ByteArrayOutputStream();
        boolean parsedHead = false;
        long size = 0;
        int c ;
        while (inputStream.ready()  && (c = inputStream.read()) != -1) {
            size++;
            if (!parsedHead) {
                if (c == '\r') {
                    out.write(c);
                    c = inputStream.read(); //read \r\n
                    if (c == '\n') {
                        out.write(c);
                        String out_ = new String(out.toByteArray());
                        if (out_.endsWith("\r\n\r\n")) {
                            String[] s1 = out_.replaceAll("\r\n", ";").split(";");
                            for (String s : s1) {
                                String[] s2;
                                if (s.contains("=\"")) {
                                    s2 = s.split("=");
                                    multipartData.getHeaders().put(s2[0].trim(), s2[1].substring(s2[1].indexOf("\"") + 1, s2[1].lastIndexOf("\"")));
                                } else {
                                    s2 = s.split(":");
                                    multipartData.getHeaders().put(s2[0].trim(), s2[1].trim());
                                }
                            }
                            out = new ByteArrayOutputStream();
                            size = 0;
                            multipartData.setName(multipartData.getHeaders().get("name"));
                            parsedHead = true;
                        }
                    }
                } else {
                    out.write(c);
                }
                continue;
            }
            currentLineOut.write(c);
            String currentLineOut_ = new String(currentLineOut.toByteArray());
            if (currentLineOut_.endsWith("\r\n")) {
                if (currentLineOut_.startsWith(boundary)) {
                    if (currentLineOut_.endsWith("--\r\n")) {
                        hasNext_ = false;
                        streamDone = true;
                    } else {
                        hasNext_ = true;
                    }
                    break;
                }
                out.write(currentLineOut.toByteArray());
                currentLineOut = new ByteArrayOutputStream();
            }
        }
        multipartData.setBody(out.toByteArray());
        return multipartData;
    }

}