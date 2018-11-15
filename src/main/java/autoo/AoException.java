package autoo;

import autoo.util.Rethrow;

public class AoException extends RuntimeException {
    public static final Rethrow<AoException> rethrow = new Rethrow<AoException>(AoException.class);
    private static final long serialVersionUID = 1L;

    public AoException() {
        super();
    }

    public AoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AoException(String message) {
        super(message);
    }

    public AoException(Throwable cause) {
        super(cause);
    }

}
