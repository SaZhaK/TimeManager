package Application.Services;

import Application.Database.UserRepository;
import Application.Entities.User;
import Application.Exceptions.UserAlreadyExistsException;
import Application.Exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public void createUser(String login, String password) throws UserAlreadyExistsException {
        if (userRepository.findUserByLoginAndPassword(login, password) != null) {
            log.info("User already exists");
            throw new UserAlreadyExistsException(login, password);
        }

        if (login != null && password != null) {
            userRepository.createUser(login, password);
        } else {
            log.error("Can not save null user");
            throw new IllegalArgumentException();
        }
    }

    public User findUserByLoginAndPassword(String login, String password) throws UserNotFoundException {
        if (login != null && password != null) {
            Optional<User> user = Optional.ofNullable(userRepository.findUserByLoginAndPassword(login, password));
            if (user.isPresent()) {
                return user.get();
            } else {
                log.info("User with login '" + login + "' and password '" + password + "' not found");
                throw new UserNotFoundException(login, password);
            }
        } else {
            log.error("Login and password can not be null");
            throw new IllegalArgumentException();
        }
    }

    public User findUserById(long id) throws UserNotFoundException {
        if (id > 0) {
            Optional<User> user = Optional.ofNullable(userRepository.findUserById(id));
            if (user.isPresent()) {
                return user.get();
            } else {
                log.info("User with id " + id + " not found");
                throw new UserNotFoundException(id);
            }
        } else {
            log.error("Incorrect id " + id);
            throw new IllegalArgumentException();
        }
    }
}
