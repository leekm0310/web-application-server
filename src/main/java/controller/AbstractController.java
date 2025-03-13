package controller;

import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();

        if (method.equals("POST")) {
            doPost(request, response);
        } else {
            doGet(request, response);
        }
    }

    void doPost(HttpRequest request, HttpResponse response) {

    }

    void doGet(HttpRequest request, HttpResponse response) {

    }

}
