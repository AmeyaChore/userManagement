package usermanagement.userManagement.repository;

public class UserNotFoundException extends Throwable {
    public UserNotFoundException(String s) {
        super(s);
    }
}
