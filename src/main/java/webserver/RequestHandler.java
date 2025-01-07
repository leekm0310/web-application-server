package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
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
            // TODO 리팩토링 절실..

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
                    response302Header(dos, body.length, "http://localhost:8080/index.html");
                    responseBody(dos, body);
                    log.debug("[회원 가입 성공]");
                }
            } else if (requestLine.equals("/user/login")) {
                String data = HttpRequestUtils.getData(br);
                if (HttpRequestUtils.findUser(data)) {
                    byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
                    response302HeaderwithLogin(dos, body.length, "/index.html", "true");
                    responseBody(dos, body);
                    log.debug("[login] : 로그인 성공");
                }
                byte[] body = Files.readAllBytes(new File("./webapp" + "/user/login_failed.html").toPath());
                response302HeaderwithLogin(dos, body.length, "/user/login_failed.html", "false");
                responseBody(dos, body);
            } else if (requestLine.equals("/user/list")) {
                String readline = br.readLine();
                while (!readline.equals("")) {
                    if (readline.contains("Cookie: ")) {
                        log.debug("쿠키라인: " + readline);

                        if (isLogin(readline)) {
                            //유저 리스트 보여준다
                            log.debug("[로그인 확인]");
                            Collection<User> userList = DataBase.findAll();
                            StringBuilder sb = new StringBuilder();
                            sb.append("<table><tbody>");
                            for (User user : userList) {
                                sb.append("</th>");
                                sb.append("<td>" + user.getUserId() + "</td>");
                                sb.append("<td>" + user.getName() + "</td>");
                                sb.append("<td>" + user.getEmail() + "</td>");
                            }
                            sb.append("</tbody></table>");
                            byte[] body = sb.toString().getBytes();
                            response200Header(dos, body.length);
                            responseBody(dos, body);
                        }
                    }
                    readline = br.readLine();
                }
                byte[] body = Files.readAllBytes(new File("./webapp" + "/user/login.html").toPath());
                response302Header(dos, body.length, "http://localhost:8080/user/login.html");
                responseBody(dos, body);
            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + requestLine).toPath());
                responseCSS(dos, body.length);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String readline) {
        String[] split = readline.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(split[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private String getRequestLine(String line) {
        String[] split = line.split(" ");
        return split[1];
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: "+ url + "\r\n");
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

    private void responseCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
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
            dos.writeBytes("Set-Cookie: logined=" + check +";");
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
