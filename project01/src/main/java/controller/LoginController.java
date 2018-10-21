package controller;

import db.DataBase;
import model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginController implements Controller {
    @Override
    public String excute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if(user!=null) {
            if(user.login(request.getParameter("password"))) {
                response.addHeader("Set-Cookie","logined=true");
                return "redirect:/index.htmll";
            } else {
                return "redirect:/user/login_failed.html";
            }
        } else {
            return "redirect:/user/login_failed.html";
        }
    }
}
