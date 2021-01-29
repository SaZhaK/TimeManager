package Application.Exceptions;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(long id) {
        super("User with id " + id + " not found");
    }
    public UserNotFoundException(String login, String password) {
        super("User with login '"+ login + "' and password '" + password + "' not found");
    }
}
