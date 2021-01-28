package Application.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class RegistrationController {

    @PostMapping("/registration")
    public ResponseEntity<String> registration(HttpServletRequest request) {
        try {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                data.append(line);
            }

            JSONObject receivedDataJson = new JSONObject(data.toString());
            String login = receivedDataJson.getString("login");
            String password = receivedDataJson.getString("password");

            log.info("Saved user for credentials " + login + ", " + password);
        } catch (Exception e) {
            log.error("unknown error: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
