# 양파껍질 협업 스터디

## 웹 서버 실습
### 요구사항1. index.html 응답하기
  - WebServer.class, RequestHandler.clas를 작성했다.
  ```java
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
            String url = token[1];
            byte[] body = null;

            if("/index.html".equals(url)) {
                body = Files.readAllBytes(new File("./webapp"+url).toPath());
            } else {
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
  ```
  - WebServer에서 사용자 요청을 받아들이고, RequestHander에서 사용자 요청에 대한 응답을 처리 한다.
  - InputStream은 클라이언트에서 서버로 요청 보낼때 사용하고, OutputStream은 서버에서 클라이언트 응답을 보낼 때 사용한다. 
  - 조사한거
    - RequestHadler에서 run()에 DataOutputStream으로 응답을 보내는데, 꼭 이걸로 해야했나?
        -  Byte를 그대로 보내려면 DataOutputStream을 사용하는 것으로 조사 했다. (다른 정보 있으면 풀리퀘 주세요.)
  - 요청 헤더를 파싱해서 요청 url을 알아내고, '/index.html'인 경우에 파일 객체를 만들고 이 파일 객체를 바이트 배열로 바꿨다.
    - 파일을 바이트 배열로 바꿀 때 Files.readAllBytes 메서드 사용.
    - Files 클래스는 jdk 1.7부터 추가가 되었다.

### 요구사항2 - Get 방식으로 회원가입 하기
  - 브라우저에서 get방식으로 form이 전송이 되면, 쿼리스트링으로 오기 때문에 url에 사용자 요청 정보가 쿼리스트링으로 포함된다.
  - 이는 값을 추출 하려면 url 문자열을 파싱('?'로 구분해서)을 해야 한다. ex) localhost:8080/user/create?userId=java&password=1234&name=ahah

### 요구사항3 - Post 방식으로 회원가입 하기
  - 브라우저에서 post 방식으로 폼이 전송이되면 url에 포함된 것이 아니라 body로 파라미터가 전송된다.
  - body를 일단 읽어야 하기 때문에, 요청헤더를 한줄 씩 읽고 빈 줄이 나오면 그 이후부터가 body가 된다. 
  - 요청 헤더를 읽을 때 BufferdReader를 사용했는데, hader는 한줄 씩 읽을 수 있으니 readLine()으로 한줄 씩 읽었다. 
  - body는 한줄씩 오는게 아니라 read()를 사용하여 케릭터 배열로 읽었다. 
  ``` java
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
  ``` 

### 요구사항4 - 302 status code 적용
  - 사용자가 정상적으로 회원가입 처리 후 User 객체가 생성이 된다면 리다이렉트 해줘야 한다.
  - 리다이렉트를 하지 않는다면, 새로고침 등으로 인한 원치 않은 결과가 실행된다.
  - 리다이렉트를 위해 응답헤더에 302코드를 추가해 주었다.
  ```java
  private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: "+"/index.html");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
  }
  ```

### 요구사항5 - 로그인하기
  - 로그인 성공 시 index.html로 이동하고, 실패 시 /user/login_failed.html로 이동한다. 
  - 로그인이 성공하면 헤더에 Cookie 정보를 Set-Cookie: logined-true로 추가해주고, 실패하면 SetCookie: logined=false로 해주었다.
  - post로 전송된 로그인 정보를 userDB에 있는 User 객체와 비교해 일치하면 로그인 성공유무를 판별하였다. 
  ```java
  public class User {
    private String userId;
    private String password;
    private String name;
    // 게터세터...
  }  
  ```
  ```java
  // RequestHander 안에 static 전역 변수 선언
  // Map으로 디비 형식으로 사용하였다.

    private static Map<String, User> userDB;
    static {
        userDB = new HashMap<>();
    }
    // ...
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
  ```

### 요구사항6 - 사용자 목록출력
  - 로그인이 됬다면 목록을 출력하게 했다. 
  - Cookie를 검사해서 logined=true 라는 형식이 오면 userDB에 있는 모든 유저의 정보를 출력하게 했다.
  - 정적인 자원이 아닌 동적자원이므로 StringBuilder를 사용하여 html 문서 형식을 동적으로 생성한 뒤 그 문자열 전체를 바이트 배열로 바꿔 전송했다. 