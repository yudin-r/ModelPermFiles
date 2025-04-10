package Utils;

import Models.User;

import java.util.HashMap;
import java.util.Map;

public class AuthSystem {
    private final Map<String, User> users;

    public AuthSystem() {
        users = new HashMap<>();
        addUser(new User("root", "root", true));
        addUser(new User("user1", "user1", false));
        addUser(new User("user2", "user2", false));
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User auth(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            System.out.println("Пользователь '" + username + "' не найден.");
            return null;
        }
        if (user.auth(password)) {
            return user;
        } else {
            System.out.println("Неверный пароль для пользователя '" + username + "'.");
            return null;
        }
    }
}