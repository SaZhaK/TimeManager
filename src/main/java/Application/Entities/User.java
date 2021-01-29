package Application.Entities;

import com.sun.istack.NotNull;
import lombok.Getter;

@Getter
public class User {
    @NotNull
    private final long id;
    @NotNull
    private final String login;
    @NotNull
    private final String password;

    public User(long id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }
}
