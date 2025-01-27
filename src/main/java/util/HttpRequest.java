package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private Map<String, String> headers = new HashMap<>();
    private String path;
    private String method;
    private Map<String, String> params = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if (line == null) {
                return;
            }

            trimRequestLine(line);
            // 그리고 한 줄 한 줄 읽으면서 진행하면 됨
            line = br.readLine();
            while (!line.equals("")) {
                String[] split = line.split(":");
                headers.put(split[0].trim(), split[1].trim());
                line = br.readLine();
            }

            if ("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-length")));
                params = HttpRequestUtils.parseQueryString(body);
            }


        } catch (IOException i) {
            log.error(i.getMessage());
        }

    }

    private void trimRequestLine(String line) {
        String[] split = line.split(" ");
        method = split[0];

        if ("POST".equals(method)) {
            path = split[1];
            return;
        }

        int idx = split[1].indexOf("?");
        if (idx == -1) {
            path = split[1];
        } else {
            path = split[1].substring(0, idx);
            params = HttpRequestUtils.parseQueryString(split[1].substring(idx + 1));
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String field) {
        return headers.get(field);
    }

    public String getParameter(String paramName) {
        return params.get(paramName);
    }

}
