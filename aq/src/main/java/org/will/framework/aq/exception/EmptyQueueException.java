package org.will.framework.aq.exception;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-30
 * Time: 11:05
 */
public class EmptyQueueException extends RuntimeException {

    private static final long serialVersionUID = 5084686378493302095L;

    public EmptyQueueException(String message) {
        super(message);
    }
}
