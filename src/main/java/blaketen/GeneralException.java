package blaketen;

public class GeneralException extends RuntimeException {
    public GeneralException(Throwable cause, String template, Object... params) {
        super(String.format(template, params), cause);
    }

    public GeneralException(String template, Object... params) {
        this(null, template, params);
    }
}
