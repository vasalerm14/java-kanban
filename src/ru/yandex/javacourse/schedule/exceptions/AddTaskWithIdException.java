package ru.yandex.javacourse.schedule.exceptions;

public class AddTaskWithIdException extends RuntimeException {
    public AddTaskWithIdException(String message) {
        super(message);
    }
}
