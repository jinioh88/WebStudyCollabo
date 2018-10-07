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
  ``` "# study" 
