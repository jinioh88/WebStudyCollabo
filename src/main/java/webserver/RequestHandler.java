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

            if(method.equals("post")) {
                User user = new User();
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
                System.out.println(user);
            }

            byte[] body = null;

            if("/index.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if("/user/form.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else if(url.startsWith("/user/create")) {


            } else  {
                body = "Hello World".getBytes();
            }

            response200Header(dos, body.length);
            responseBody(dos, body);
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: "+lengthOfBodyContent+"\r\n");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

}
