package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;
    private static Map<String, User> userDB;
    static {
        userDB = new HashMap<>();
    }

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try(InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String[] token = br.readLine().split(" ");
            String method = token[0].toLowerCase();
            String url = token[1];

            String line="";
            Map<String,String> headerMap = new HashMap<>();

            while((line=br.readLine())!=null) {
                if("".equals(line))
                    break;
                String[] temp = line.split("[:]");
                headerMap.put(temp[0],temp[1].trim());
            }

            String contentType = "text/html";
            String cookie = "";

            if(method.equals("post")) {
                User user = new User();
                if(url.startsWith("/user/create")) {
                    int len2 = Integer.parseInt(headerMap.get("Content-Length"));
                    char[] body = new char[len2];
                    br.read(body,0,len2);

                    String params = String.copyValueOf(body);
                    String[] str = params.split("[&]");
                    for (String s : str) {
                        String[] res = s.split("=");
                        String key = res[0];
                        String val = res[1];
                        if ("userId".equals(key)) {
                            user.setUserId(val);
                        }
                        if ("password".equals(key)) {
                            user.setPassword(val);
                        }
                        if ("name".equals(key)) {
                            user.setName(val);
                        }
                    }
                    userDB.put(user.getUserId(), user);
                    response302Header(dos);
                    return;
                }

                if(url.startsWith("/user/login")) {
                    int len2 = Integer.parseInt(headerMap.get("Content-Length"));
                    char[] body = new char[len2];
                    br.read(body,0,len2);

                    String params = String.copyValueOf(body);
                    String[] str = params.split("[&]");
                    String userId = "";
                    String password = "";
                    for (String s : str) {
                        String[] res = s.split("=");
                        String key = res[0];
                        String val = res[1];
                        if ("userId".equals(key)) {
                            userId = val;
                        }
                        if ("password".equals(key)) {
                            password = val;
                        }
                    }
                    if (userDB.containsKey(userId)) {
                        String password2 = userDB.get(userId).getPassword();
                        if (password.equals(password2)) {
                            cookie = "logined=true";
                            url = "/index.html";
                        }
                    } else {
                        cookie = "logined=false";
                        url = "/user/login_failed.html";
                    }
                }
            }

            byte[] body = null;

            if("/index.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if("/user/form.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if("/user/login.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if("/user/login_failed.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if("/user/list".equals(url)) {
                Boolean logined = false;
                String reqCookie = headerMap.get("Cookie");
                String[] cookies = reqCookie.split(";");
                for (String c : cookies) {
                    if (c.startsWith("logined=")) {
                        String s = c.split("=")[1];
                        logined = Boolean.parseBoolean(s);
                    }
                }
                if (logined) {
                    body = getUserList();
                } else {
                    url = "/user/login.html";
                    body = Files.readAllBytes(new File("./webapp"+url).toPath());
                }
            } /*else if (url.endsWith(".css")) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
                contentType = "text/css";
            }*/ else {
                body = "Hello World".getBytes();
            }

            response200Header(dos, body.length, cookie, contentType);
            responseBody(dos, body);
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] getUserList() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>\n" +
                "<html lang=\"kr\">\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n");

        for (String id : userDB.keySet()) {
            builder.append("<tr>\n" +
                            "<td>"+userDB.get(id).getName()+"</td>" +
                            "</tr>\n");
        }

        builder.append("</table>\n" +
                "</body>\n" +
                "</html>");

        return builder.toString().getBytes();
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: "+"/index.html");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body,0,body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cookie, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: "+lengthOfBodyContent+"\r\n");
            if (!"".equals(cookie)) {
                dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

}
