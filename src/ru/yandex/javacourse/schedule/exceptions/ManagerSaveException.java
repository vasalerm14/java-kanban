package ru.yandex.javacourse.schedule.exceptions;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
