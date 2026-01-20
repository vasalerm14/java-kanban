package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskIntersectionTest {

    @Test
    @DisplayName("Задачи пересекаются по времени — должно выбрасываться исключение")
    void shouldThrowExceptionWhenTasksIntersect() {
        TaskManager manager = new InMemoryTaskManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 30));
        task2.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(task1);
        assertThrows(
                IllegalStateException.class,
                () -> manager.addNewTask(task2),
                "Пересекающиеся задачи должны вызывать исключение"
        );
    }

    @Test
    @DisplayName("Задачи не пересекаются по времени — исключения быть не должно")
    void shouldNotThrowExceptionWhenTasksDoNotIntersect() {
        TaskManager manager = new InMemoryTaskManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2026, 1, 1, 11, 0));
        task2.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(task1);
        assertDoesNotThrow(
                () -> manager.addNewTask(task2),
                "Непересекающиеся задачи не должны вызывать исключение"
        );
    }
}
