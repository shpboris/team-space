package org.teamspace.data_import.exceptions;

/**
 * Created by shpilb on 21/10/2017.
 */
public class DataImportException extends RuntimeException {

    public DataImportException(String message) {
        super(message);
    }

    public DataImportException(String message, Throwable t) {
        super(message, t);
    }
}
