package ru.yandex.javacourse.schedule.exceptions;

public class ManageBackupException extends RuntimeException {
    public ManageBackupException(String message, final Throwable cause) {
        super(message, cause);
    }
}
