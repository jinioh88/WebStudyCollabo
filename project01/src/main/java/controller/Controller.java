package controller;

import com.sun.deploy.net.HttpResponse;
import http.HttpRequest;

public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
