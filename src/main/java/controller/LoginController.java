package controller;

import db.DataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;

public class LoginController implements Controller{

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        log.debug("나야 로그인 컨트롤러");
        String id = request.getParameter("userId");
        if (DataBase.findUserById(id) == null) {
            response.addHeader("Set-Cookie", "logined=false");
            response.redirect("/user/login_failed.html");
            return;
        }
        log.debug("[login] : 로그인 성공");
        response.addHeader("Set-Cookie", "logined=true");
        response.redirect("/index.html");
    }
}
