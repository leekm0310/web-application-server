package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String targetLine = br.readLine();
            String requestLine = getRequestLine(targetLine);
            log.debug("[requestLine]: " + requestLine);

            if (requestLine.contains(".html")) {
                byte[] body = Files.readAllBytes(new File("./webapp" + requestLine).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else if (requestLine.contains("/user/create")) {
                String data = HttpRequestUtils.getData(br);
                if (HttpRequestUtils.saveUser(data)) {
                    byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
                    response302Header(dos, body.length);
                    responseBody(dos, body);
                }
            } else if (requestLine.equals("/user/login")) {
                String data = HttpRequestUtils.getData(br);
                if (HttpRequestUtils.findUser(data)) {
                    byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
                    response302HeaderwithLogin(dos, body.length, "/index.html", "true");
                    responseBody(dos, body);
                }
                byte[] body = Files.readAllBytes(new File("./webapp" + "/user/login_failed.html").toPath());
                response302HeaderwithLogin(dos, body.length, "/user/login_failed.html", "false");
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getRequestLine(String line) {
        String[] split = line.split(" ");
        return split[1];
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent+ "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderwithLogin(DataOutputStream dos, int lengthOfBodyContent, String url, String check) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: http://localhost:8080" + url +"\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + check);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
