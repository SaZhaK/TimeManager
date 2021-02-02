package Application.Controllers;

import Application.Entities.User;
import Application.Exceptions.UserAlreadyExistsException;
import Application.Exceptions.UserNotFoundException;
import Application.Security.JWTProvider;
import Application.Services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Slf4j
@RestController
public class UserController {

    @Autowired
    UserService userService;

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

            if (login.length() < 8) {
                log.info("short login");
                return new ResponseEntity<>("login has to contain at least 8 symbols", HttpStatus.LENGTH_REQUIRED);
            }
            if (login.length() > 32) {
                log.info("long login");
                return new ResponseEntity<>("login has to be less than 32 symbols", HttpStatus.LENGTH_REQUIRED);
            }
            if (password.length() < 8) {
                log.info("short password");
                return new ResponseEntity<>("password has to contain at least 8 symbols", HttpStatus.LENGTH_REQUIRED);
            }
            if (password.length() > 32) {
                log.info("long password");
                return new ResponseEntity<>("password has to be less than 32 symbols", HttpStatus.LENGTH_REQUIRED);
            }
            userService.createUser(login, password);
        } catch (IllegalArgumentException e) {
            log.error("illegal argument: {}", e.getMessage());
            return new ResponseEntity<>("request contains wrong data", HttpStatus.BAD_REQUEST);
        } catch (UserAlreadyExistsException e) {
            log.error("user already exists: {}", e.getMessage());
            return new ResponseEntity<>("user with this credentials already exists", HttpStatus.CONFLICT);
        } catch (JSONException e) {
            log.error("json exception: {}", e.getMessage());
            return new ResponseEntity<>("required fields not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("unknown exception: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create("https://holidaytodo.herokuapp.com/registration"));
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @GetMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest request) {
        String token;
        try {
            String login = request.getParameter("login");
            String password = request.getParameter("password");

            User user = userService.findUserByLoginAndPassword(login, password);

            token = JWTProvider.generateToken(user.getId(), login, password);
        } catch (IllegalArgumentException e) {
            log.error("illegal argument: {}", e.getMessage());
            return new ResponseEntity<>("request contains wrong data", HttpStatus.NOT_FOUND);
        } catch (UserNotFoundException e) {
            log.error("user not found: {}", e.getMessage());
            return new ResponseEntity<>("user not found", HttpStatus.NOT_FOUND);
        } catch (JSONException e) {
            log.error("json exception: {}", e.getMessage());
            return new ResponseEntity<>("required fields not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("unknown exception: {}", e.getMessage());
            return new ResponseEntity<>("something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create("https://holidaytodo.herokuapp.com/login"));
        responseHeaders.set("Authorization", "Bearer " + token);
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
}
