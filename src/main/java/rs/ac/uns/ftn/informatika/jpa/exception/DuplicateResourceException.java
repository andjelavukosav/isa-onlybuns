package rs.ac.uns.ftn.informatika.jpa.exception;

public class DuplicateResourceException extends RuntimeException {
    private String field;

    public DuplicateResourceException(String message, String field) {
        super(message);
        this.field = field;
    }


    public String getField() {
        return field;
    }
    public DuplicateResourceException(String message) {
        super(message);
    }
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
