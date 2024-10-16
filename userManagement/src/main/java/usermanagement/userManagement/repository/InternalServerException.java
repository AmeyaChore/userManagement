package usermanagement.userManagement.repository;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String internalServerError) {
        super(internalServerError);
    }
}
