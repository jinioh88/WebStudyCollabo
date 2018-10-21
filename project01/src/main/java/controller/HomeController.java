package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HomeController implements Controller{
    @Override
    public String excute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "home.jsp";
    }
}
