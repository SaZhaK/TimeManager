package Application.Database;

import Application.Entities.User;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbc;

    private static final ResultSetExtractor<User> USER_MAPPER = resultSet -> {
        User user = !resultSet.next() ? null :
                new User(
                        resultSet.getLong("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"));

        if (resultSet.next()) {
            throw new RuntimeException("Multiple users found");
        }

        return user;
    };

    public void createUser(@NotNull String login, @NotNull String password) {
        jdbc.update("INSERT INTO users (login, password) VALUES (?,?)",
                login,
                password);

    }

    public User findUserByLoginAndPassword(@NotNull String login, @NotNull String password) {
        return jdbc.query("SELECT id, login, password FROM users WHERE login=? and password=?",
                USER_MAPPER,
                login,
                password);
    }

    public User findUserById(long id) {
        return jdbc.query("SELECT id, login, password FROM users WHERE id =?",
                USER_MAPPER,
                id);
    }
}
