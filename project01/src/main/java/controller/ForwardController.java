package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardController implements Controller {
    private String forwardUrl;

    public ForwardController(String forwardUrl) {
        this.forwardUrl = forwardUrl;
        if(forwardUrl==null) {
            throw new NullPointerException("forwardUrl is null");
        }
    }

    @Override
    public String excute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return forwardUrl;
    }
}
