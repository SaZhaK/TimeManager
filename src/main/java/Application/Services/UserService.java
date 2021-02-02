package Application.Services;

import Application.Cache.UserCache;
import Application.Database.UserRepository;
import Application.Entities.User;
import Application.Exceptions.UserAlreadyExistsException;
import Application.Exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * This class represents an abstract layer between controllers and database
 * Should be used for getting any information on users
 *
 * <p> Uses {@see UserCache} for caching information and obtaining it from fast access memory instead of database if possible
 *
 * <p> Logs additional information on IllegalArgumentException on ERROR level,
 * UserAlreadyExistsException and UserNotFoundException on INFO level
 *
 * @author sazha
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    /**
     * Creates new user with given credentials and caches it
     *
     * @param login    - login to be assigned for a new user
     * @param password - password to be assigned for a new user
     * @throws UserAlreadyExistsException if ResultSet returned by query for user with such login and password was not empty
     * @throws IllegalArgumentException   if login or data were null
     */
    public void createUser(String login, String password) throws UserAlreadyExistsException {
        if (login != null && password != null) {
            if (userRepository.findUserByLoginAndPassword(login, password) != null) {
                log.info("User already exists");
                throw new UserAlreadyExistsException(login, password);
            }

            User user = userRepository.createUser(login, password);
            UserCache.cacheUser(user);
        } else {
            log.error("Can not save user with null credentials");
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns a user with given login and password
     * Should be used only for login process, for other purposes use {@link #findUserById(long id)} method
     * Does not check if requested user was already cached, but caches th result of query to database
     *
     * @param login    - user login in database
     * @param password - user password in database
     * @throws UserNotFoundException    containing requested login and password if ResultSet
     *                                  returned by query for user with such login and password was empty
     * @throws IllegalArgumentException if login or data were null
     */
    public User findUserByLoginAndPassword(String login, String password) throws UserNotFoundException {
        if (login != null && password != null) {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findUserByLoginAndPassword(login, password));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserCache.cacheUser(user);
                return user;
            } else {
                log.info("User with login '" + login + "' and password '" + password + "' not found");
                throw new UserNotFoundException(login, password);
            }
        } else {
            log.error("Login and password can not be null");
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns a user with given id
     * Should be preferred to {@link #findUserByLoginAndPassword(String login, String password)} method
     * First checks if user was cached, otherwise performs a query to database and caches the result
     *
     * @param id - user id
     * @throws UserNotFoundException    containing requested id if ResultSet returned by query for user with such id was empty
     * @throws IllegalArgumentException if id was negative or zero
     */
    public User findUserById(long id) throws UserNotFoundException {
        if (id >= 0) {
            User cachedUser = UserCache.getUser(id);
            if (cachedUser != null) {
                return cachedUser;
            }

            Optional<User> userOptional = Optional.ofNullable(userRepository.findUserById(id));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserCache.cacheUser(user);
                return user;
            } else {
                log.info("User with id " + id + " not found");
                throw new UserNotFoundException(id);
            }
        } else {
            log.error("Incorrect id " + id);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Deletes user with given id from database and cache if present
     * Deleting process is guaranteed to be successful if no exceptions were thrown from this method
     *
     * @param id - user id
     * @throws UserNotFoundException    containing requested id if ResultSet returned by query for user with such id was empty
     * @throws IllegalArgumentException if id was negative or zero
     */
    public void deleteUser(long id) throws UserNotFoundException {
        if (id >= 0) {
            if (userRepository.deleteUserById(id)) {
                UserCache.removeUser(id);
                log.info("User with id " + id + " was deleted");
            } else {
                log.info("User with id " + id + " not found");
                throw new UserNotFoundException(id);
            }
        } else {
            log.error("Incorrect id " + id);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns all users from database
     */
    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
