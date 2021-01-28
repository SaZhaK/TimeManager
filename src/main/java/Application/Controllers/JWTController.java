package Application.Controllers;

import Application.Security.JWTProvider;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
public class JWTController {

    @GetMapping("/token")
    public String getToken(HttpServletRequest request) {
        String token = "init";
        try {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                data.append(line);
            }

            JSONObject receivedDataJson = new JSONObject(data.toString());
            String login = receivedDataJson.getString("login");
            String password = receivedDataJson.getString("password");

            token = JWTProvider.generateToken(login, password);
        } catch (IOException e) {
            log.error("io exception error: {}", e.getMessage());
        }
        System.out.println(JWTProvider.getLogin(token));

        return token;
    }
}
