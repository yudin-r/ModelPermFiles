package Models;

public class User {
    private final String username;
    private final String password;
    private final boolean isRoot;

    public User(String username, String password, boolean isRoot) {
        this.username = username;
        this.password = password;
        this.isRoot = isRoot;
    }

    public String getUsername() {
        return username;
    }

    public boolean auth(String password) {
        return this.password.equals(password);
    }

    public boolean isRoot() {
        return isRoot;
    }
}