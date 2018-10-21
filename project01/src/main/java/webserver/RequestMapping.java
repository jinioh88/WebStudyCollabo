package webserver;

import controller.*;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static Map<String, Controller> controllers = new HashMap<>();
    private Map<String, Controller> mappings = new HashMap<>();

    public void initMapping() {
        mappings.put("/",new HomeController());
        mappings.put("/users/form", new ForwardController("/user/form.html"));
        mappings.put("/users/loginForm",new ForwardController("/user/login.html"));
        mappings.put("/users", new ListUserController());
        mappings.put("/users/login", new LoginController());
//        mappings.put("/users/frofile", new ProfileController());
//        mappings.put("/users/logout", new LogoutController());
        mappings.put("/users/create", new CreateUserController());
//        mappings.put("/users/updateForm", new UpdateFormUserController());
//        mappings.put("/users/update", new UpdateUserController());

    }

    public Controller findController(String url) {
        return mappings.get(url);
    }

//    public static Controller getController(String requestUrl) {
//        return controllers.get(requestUrl);
//    }
    void put(String url, Controller controller) {
        mappings.put(url,controller);
    }
}
