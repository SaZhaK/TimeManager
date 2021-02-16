package Application.Controllers;

import Application.Entities.User;
import Application.Exceptions.UserAlreadyExistsException;
import Application.Exceptions.UserNotFoundException;
import Application.Security.JWTProvider;
import Application.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Slf4j
@RestController
@Tag(name = "Авторизация", description = "Методы для работы с авторизацией пользователей")
public class UserController {

    @Autowired
    UserService userService;

    @Operation(summary = "Регистрация", description = "Используется для регистрации нового пользователя",
            requestBody = @RequestBody(required = true, description = "contains login and password for new user",
                    content = @Content(mediaType = "JSON", schema = @Schema(requiredProperties = {"login", "password"},
                            example = "{login:\"login\", password:\"password\"}"))))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "created",
                    content = @Content(mediaType = "Text"),
                    headers = @Header(name = "Location", description = "server URL")),
            @ApiResponse(responseCode = "400", description = "wrong data",
                    content = @Content(mediaType = "Text", examples = {
                            @ExampleObject(value = "request contains wrong data"),
                            @ExampleObject(value = "required fields not found")})),
            @ApiResponse(responseCode = "409", description = "user with this credentials already exists",
                    content = @Content(mediaType = "Text", examples = @ExampleObject(value = "user not found"))),
            @ApiResponse(responseCode = "411", description = "incorrect length for login and password",
                    content = @Content(mediaType = "Text", examples = {
                            @ExampleObject(value = "login has to contain at least 8 symbols"),
                            @ExampleObject(value = "login has to be less than 32 symbols"),
                            @ExampleObject(value = "password has to contain at least 8 symbols"),
                            @ExampleObject(value = "password has to be less than 32 symbols")
                    })),
            @ApiResponse(responseCode = "500", description = "unknown error",
                    content = @Content(mediaType = "Text", examples = @ExampleObject(value = "something went wrong")))
    })
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
            return new ResponseEntity<>("something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(URI.create("https://holidaytodo.herokuapp.com/registration"));
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @GetMapping("/login")
    @Operation(summary = "Логин", description = "Используется для входа в систему")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "success",
                    content = @Content(mediaType = "Text"),
                    headers = {
                            @Header(name = "Authorization", description = "authorization JWT", schema = @Schema(format = "Bearer <JWT>")),
                            @Header(name = "Location", description = "server URL")
                    }),
            @ApiResponse(responseCode = "400", description = "wrong parameters",
                    content = @Content(mediaType = "Text", examples = @ExampleObject(value = "request contains wrong parameters"))),
            @ApiResponse(responseCode = "404", description = "user not found",
                    content = @Content(mediaType = "Text", examples = @ExampleObject(value = "user not found"))),
            @ApiResponse(responseCode = "500", description = "unknown error",
                    content = @Content(mediaType = "Text", examples = @ExampleObject(value = "something went wrong")))
    })
    public ResponseEntity<String> login(@RequestParam("login")
                                        @Parameter(description = "Логин пользователя, не менее 8 и не более 32 символов в длину",
                                                required = true,
                                                schema = @Schema(minLength = 8, maxLength = 32))
                                                String login,
                                        @RequestParam("password")
                                        @Parameter(description = "Пароль пользователя, не менее 8 и не более 32 символов в длину",
                                                required = true,
                                                schema = @Schema(minLength = 8, maxLength = 32))
                                                String password) {
        String token;
        try {
            User user = userService.findUserByLoginAndPassword(login, password);

            token = JWTProvider.generateToken(user.getId(), login, password);
        } catch (IllegalArgumentException e) {
            log.error("illegal argument: {}", e.getMessage());
            return new ResponseEntity<>("request contains wrong parameters", HttpStatus.BAD_REQUEST);
        } catch (UserNotFoundException e) {
            log.error("user not found: {}", e.getMessage());
            return new ResponseEntity<>("user not found", HttpStatus.NOT_FOUND);
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
