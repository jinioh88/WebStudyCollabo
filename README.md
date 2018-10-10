# 양파껍질 협업 스터디

## 문자열 계산기 구현을 통한 통합 테스트와 리펙토링
### main() 메서드를 활용한 테스트의 문제점
  ```java
  public class Calculator {
    int add(int i, int j) {
        return i+j;
    }

    int subtract(int i, int j) {
        return i-j;
    }

    int multiply(int i, int j) {
        return i*j;
    }

    int divide(int i, int j) {
        return i/j;
    }

    public static void main(String[] args) {
        Calculator cal = new Calculator();
        System.out.println(cal.add(3,4));
        System.out.println(cal.subtract(5,3));
        System.out.println(cal.multiply(2,5));
        System.out.println(cal.divide(6,2));
    }
  }
  ```
  - main()은 두가지 목적으로 나뉜다.
    - 첫번째는 프로그래밍을 실행하기 위한 목적
    - 두번째는 프로덕션 코드가 정상 동작 하는지 테스트 목적
  - 위 코드의 문제점은 프로덕션 코드와 테스트 코드가 같은 클래스에 위치한다는 것이다. 
  - 테스트 코드를 따로 분리 해 보자.
  ```java
  public class CalculatorTest {
    public static void main(String[] args) {
        Calculator cal = new Calculator();
        System.out.println(cal.add(3,4));
        System.out.println(cal.subtract(5,3));
        System.out.println(cal.multiply(2,5));
        System.out.println(cal.divide(6,2));
    }
  }
  ```
  - 위 방식은 메인이 복잡하면 복잡해 질수록 유지 보수에 부담이 된다. 
  ```java
  public class CalculatorTest {
    public static void main(String[] args) {
        Calculator cal = new Calculator();
        add(cal);
        subtract(cal);
        multiply(cal);
        divide(cal);

    }

    private static void add(Calculator cal) {
        System.out.println(cal.add(3,4));

    }

    private static void subtract(Calculator cal) {
        System.out.println(cal.subtract(5,3));

    }

    private static void multiply(Calculator cal) {
        System.out.println(cal.multiply(2,5));

    }

    private static void divide(Calculator cal) {
        System.out.println(cal.divide(6,2));

    }
  }
  ```
  - 위 방식은 모든 테스트를 해야 하는 불합리한 작업이다.
  - 클래스가 가지고 있는 모든 메서드에 관심이 있는게 아니라 현재 내가 구현하고 있는 메서드에만 집중하고 싶다. 
  - 또 다른 문제는 테스트 결과를 항상 콘솔에 출력해 수동으로 확인하는 점이다. 
  - 위 문제를 해결하기 위해 JUnit을 사용한다. 

### JUnit을 활용해 main() 메서드 문제점 극복
  - 단 한번에 메서드 하나에만 집중
  ```java
  public class CalculatorTest {
    @Test
    public void add() {
        Calculator cal = new Calculator();
        System.out.println(cal.add(6,3));
    }

    @Test
    public void subtract() {
        Calculator cal = new Calculator();
        System.out.println(cal.subtract(3,1));
    }
  }
  ```
    - 위와 같이 구현하면 각각의 테스트 메서드를 독립적으로 실행할 수 있다. 
    - 하지만 위 문제는 실행결과를 직접 눈으로 확인해야 한다. 
  - 결과 값을 눈이 아닌 프로그램을 통해 자동화
    - JUnit은 assertEquals() 메서드를 제공한다. 
  ```java
  import org.junit.Test;
  import static org.junit.Assert.assertEquals;

  public class CalculatorTest {
    @Test
    public void add() {
        Calculator cal = new Calculator();
        assertEquals(9, cal.add(6,3));
    }

    @Test
    public void subtract() {
        Calculator cal = new Calculator();
        assertEquals(1, cal.subtract(6,5));
    }
  }
  ```
  - assertEquals(), assertTrue(), assertFalse(), assertNull(), assertNotNull() 등이 있다. 
  - JUnit에서 초기화는 @Before 에노테이션을 활용하는걸 추천한다.
  ```java
    private Calculator cal;
    
    @Before
    public void setup() {
        cal = new Calculator();
    }
  ```    

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
  
### 요구사항 7 - css 지원하기
  - 브라우저가 서버에 자원을 요청 할 때, 모든 정적 자원을 각각 요청하게 된다.
  - 정적자원에는 html만 있는 게 아니라, css, javascript, image 등등이 있다.
  - 이 content type들을 모두 지원해줘야 한다.
  - css를 지원하기 위해 먼저 url 주소가 ".css"로 끝나는 요청이 오면 해당 css 파일을 바이트 배열로 바꿔서 body에 담았다.
  - 기존의 헤더메소드를 사용하면 응답 헤더에 content type이 "text/html"로 가기때문에 브라우저에서 응답받은 파일이 html인줄 알고 css로 렌더링하지 않는다.
  - 그렇기 때문에 응답 헤더에 content type을 "text/css"로 변경해서 응답하도록 메소드를 수정하였다.
  ```java
  // css 요청인지 판별해서 body에 css파일을 담아주고 헤더 정보를 바꿔주는 부분
  else if (url.endsWith(".css")) {
      body = Files.readAllBytes(new File("./webapp"+url).toPath());
      contentType = "text/css";
  }
  ```
  ```java
  // 하드코딩 되어있던 content type 부분을 String contentType 파라미터로 받아서 전달시키도록 수정하였다. 
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
  ```  