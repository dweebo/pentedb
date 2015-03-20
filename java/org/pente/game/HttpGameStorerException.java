package org.pente.game;

/**
 * @author dweebo
 */
public class HttpGameStorerException extends Exception {

    private Exception exception;

    public HttpGameStorerException() {
        super();
    }

    public HttpGameStorerException(String message) {
        super(message);
    }

    public HttpGameStorerException(String message, Exception ex) {
        super(message);

        this.exception = ex;
    }

    public Exception getBaseException() {
        return exception;
    }
}