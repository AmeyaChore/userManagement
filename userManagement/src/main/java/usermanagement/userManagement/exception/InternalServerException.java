package usermanagement.userManagement.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String internalServerError) {
        super(internalServerError);
    }
}
