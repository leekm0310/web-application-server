package webserver;

import java.io.*;
import java.net.HttpCookie;
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
            byte[] body = readInputStream(in); // TODO 이렇게 하지 말고 바꿔야 된다. 응답 헤더 보내야 하니까
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] readInputStream(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        log.debug("[line] :" + line);
        String[] split = line.split(" ");
        String requestLine = split[1];
        if (requestLine.contains(".html")) {
            return Files.readAllBytes(new File("./webapp" + requestLine).toPath());
        } else if (requestLine.contains("/user/create")) {
            String data = HttpRequestUtils.getData(br);
            if (HttpRequestUtils.saveUser(data)) {
                return Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
            }
        } else if (requestLine.contains("/user/login")) {
            String data = HttpRequestUtils.getData(br);
            if (HttpRequestUtils.findUser(data)) {

            }
        }
        return Files.readAllBytes(new File("./webapp" + requestLine).toPath()); // 수정 필요
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
