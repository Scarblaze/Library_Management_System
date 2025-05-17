// Student.java
package model;

public class Student extends User {
    public Student(int userId, String name, long mobile) {
        super(userId, name, true, mobile);
    }
}
