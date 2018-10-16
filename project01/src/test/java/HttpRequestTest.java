
import http.HttpRequest;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    private String testDir = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDir + "Http_Get.txt"));
        HttpRequest request = new HttpRequest(in);
        System.out.println(request.getMethod());
        assertEquals("GET", request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeaders("Connection"));
        assertEquals("sanazzang",request.getParameter("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDir+"Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals("POST",request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive",request.getHeaders("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));
    }
}
