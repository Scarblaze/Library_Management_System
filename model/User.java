// User.java
package model;

public class User {
    protected int userId;
    protected String name;
    protected boolean isStudent;
    protected long mobile;

    public User(int userId, String name, boolean isStudent, long mobile) {
        this.userId = userId;
        this.name = name;
        this.isStudent = isStudent;
        this.mobile = mobile;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public long getMobile() {
        return mobile;
    }
}
