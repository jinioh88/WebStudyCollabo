
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpRequestUtils;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connecionSocket) {
        this.connection = connecionSocket;
    }

    @Override
    public void run() {
        try(InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out)
            String path = getDefualtPath(request.getPath());

            if("/user/create".equals(path)) {
                User user = new User(request.getParameter("userId"), request.getParameter("password"),
                        request.getParameter("name"), request.getParameter("email"));
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            } else if("/user/login".equals(path)) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if(user!=null) {
                    responseResource(out, "/user/login_failed.html");
                    return;
                }
                if(user.getPassword().equals(request.getParameter("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource(out, "/user/login_failed.html");
                }
            } else if("/user/list".equals(path)) {
                if(!isLogin(request.getHeaders("Cookie"))) {
                    responseResource(out,"/user/login.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for(User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>"+user.getUserId()+"</td>");
                    sb.append("<td>"+user.getName()+"</td>");
                    sb.append("<td>"+user.getEmail()+"</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos,body.length);
                responseBody(dos,body);
<<<<<<< HEAD
            } else if(path.endsWith(".css")) {
                responseCssResource(out,path);
=======
>>>>>>> parent of 42bc0fe... 요구사항7 - 책버전
            } else {
                responseResource(out, path);
            }
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

<<<<<<< HEAD

    private String getDefualtPath(String path) {
        if(path.equals("/")) {
            return "index.html";
        }
        return path;
    }

    private void response200CSSHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: "+lengthOfBodyContent+"\r\n");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String cookieValue) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
=======
    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
>>>>>>> parent of 42bc0fe... 요구사항7 - 책버전
        String value = cookies.get("logined");
        if(value==null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: "+ url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }




}
