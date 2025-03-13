package controller;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        //로그인 되어 있는지 확인 - 쿠키로
        if (!isLogin(request.getHeader("Cookie"))) {
            response.redirect("/user/login.html");
            return;
        }
        // 로그인 되어 있으면 로그인 유저 리스트 보여줌
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
        response.forwardBody(sb.toString());
    }

    boolean isLogin(String line) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(line);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
