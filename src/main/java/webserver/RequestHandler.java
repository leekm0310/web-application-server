package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

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

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String path = request.getPath();

            if ("/user/create".equals(path)) {
                User user = new User(request.getParameter("userId"), request.getParameter("password"),
                        request.getParameter("name"), request.getParameter("mail"));
                DataBase.addUser(user);
                log.debug("[회원 가입 성공] : {}", user.getUserId());
                response.redirect("/index.html");

            } else if ("/user/login".equals(path)) {
                String id = request.getParameter("userId");
                User userById = DataBase.findUserById(id);
                log.debug("[user] : {}", userById.getUserId());
                if (userById.getUserId().equals(id)) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.redirect("/index.html");
                    log.debug("[login] : 로그인 성공");
                } else {
                    response.addHeader("Set-Cookie", "logined=false");
                    response.redirect("/user/login_failed.html");
                }

            } else if ("/user/list".equals(path)) {

                if (!isLogin(request.getHeader("Cookie"))) {
                    response.redirect("/user/login.html");
                    return;
                }

                Collection<User> userList = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table><tbody>");
                for (User user : userList) {
                    sb.append("</th>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                }
                response.forwardBody(sb.toString());
            } else {
                response.forward(path);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String readline) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(readline);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
