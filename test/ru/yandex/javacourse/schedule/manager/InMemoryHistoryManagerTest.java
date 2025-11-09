package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions() {
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
    }

    @Test
    public void testHistoricVersionsByPointer() {
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().get(0).getStatus(), "historic task should not be changed");
    }

    @Test
    public void testHistoricUnlimitedSize() {
        for (int i = 0; i < 20; i++) {
            Task task = new Task(i, "Task № " + i, "Test task", TaskStatus.NEW);
            historyManager.addTask(task);
        }
        assertEquals(20, historyManager.getHistory().size());
    }

    @Test
    public void TestHistoryRemove() {
        Task task1 = new Task(1, "Task 1", "Test task", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 1", "Test task", TaskStatus.NEW);
        Task task3 = new Task(3, "Task 1", "Test task", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(2);
        assertEquals(2, historyManager.getHistory().size());
    }

}
