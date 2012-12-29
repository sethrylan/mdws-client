package gov.va.iehr.mdws.exception;

/**
 *
 * @author gaineys
 */
@SuppressWarnings("serial")
public class NoConnectionException extends RuntimeException{
    
    
    /**
     * Instantiates a new NoConnectionException.
     *
     * @param message
     *            the exception message
     */
    public NoConnectionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new NoConnectionException.
     *
     * @param message
     *            the exception message
     * @param cause
     *            the exception cause.
     */
    public NoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    
    /**
     * Instantiates a new NoConnectionException.
     *
     * @param cause
     *            the exception cause.
     */
    public NoConnectionException(Throwable cause) {
        super(cause);
    }
}
