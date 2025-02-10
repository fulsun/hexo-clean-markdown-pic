package pers.fulsun.cleanup.exception;

import java.io.IOException;

public class TaskException extends RuntimeException {
    public TaskException(String message) {
        super(message);
    }
    public TaskException(IOException e) {
        super(e);
    }

    public TaskException(String s, IOException e) {
        super(s, e);
    }

    public TaskException(String s, RuntimeException e) {
        super(s, e);
    }

    public TaskException(String msg, InterruptedException e) {
        super(msg, e);
    }
}
