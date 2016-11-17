package com.di.nomothesia;

/**
 * Base application-level Exception
 * All application exceptions should extend this class.
 *
 * Created by psour on 14/11/2016.
 */
public class NomothesiaException extends Exception {
    private static final long serialVersionUID = 1L;

    public NomothesiaException() {
        super();
    }

    public NomothesiaException(String message, Throwable cause) {
        super(message, cause);
    }

    public NomothesiaException(String message) {
        super(message);
    }

    public NomothesiaException(Throwable cause) {
        super(cause);
    }
}
