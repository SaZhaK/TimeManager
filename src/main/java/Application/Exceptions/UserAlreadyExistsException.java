package Application.Exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String login, String password) {
        super("User with login '" + login + "' and password '" + password + "' already exists");
    }
}
