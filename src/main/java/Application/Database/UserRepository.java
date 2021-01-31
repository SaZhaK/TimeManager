package Application.Database;

import Application.Entities.User;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;

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

    public User createUser(@NotNull String login, @NotNull String password) {
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement statement = con.prepareStatement("INSERT INTO users (login, password) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, login);
            statement.setString(2, password);
            return statement;
        }, holder);

        long id = holder.getKey().longValue();

        return new User(id, login, password);
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
