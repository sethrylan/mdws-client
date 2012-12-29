package gov.va.iehr.mdws.exception;

/**
 *
 * @author gaineys
 */
public class NoSiteException extends RuntimeException {
    
    
    /**
     * Instantiates a new NoSiteException.
     *
     * @param message
     *            the exception message
     */
    public NoSiteException(String message) {
        super(message);
    }

    /**
     * Instantiates a new NoSiteException.
     *
     * @param message
     *            the exception message
     * @param cause
     *            the exception cause.
     */
    public NoSiteException(String message, Throwable cause) {
        super(message, cause);
    }

    
    /**
     * Instantiates a new NoSiteException.
     *
     * @param cause
     *            the exception cause.
     */
    public NoSiteException(Throwable cause) {
        super(cause);
    }
}
