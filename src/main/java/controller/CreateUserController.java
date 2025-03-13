package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;

public class CreateUserController extends AbstractController{

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        //TODO: 유저 빌더 패턴 적용해보기
        log.debug("[called] : UserCreateController");
        User user = new User(request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("mail"));
        DataBase.addUser(user);
        log.debug("[user] : {}", user);

        response.redirect("/index.html");
    }
}
