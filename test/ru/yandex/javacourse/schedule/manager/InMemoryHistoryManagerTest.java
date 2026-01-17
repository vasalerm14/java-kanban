package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager history;

    @BeforeEach
    void init() {
        history = Managers.getDefaultHistory();
    }

    @Test
    void emptyHistory() {
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    void noDuplicates() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW);
        history.addTask(task);
        history.addTask(task);
        assertEquals(1, history.getHistory().size());
    }

    @Test
    void removeFromBeginning() {
        Task t1 = new Task(1, "1", "", TaskStatus.NEW);
        Task t2 = new Task(2, "2", "", TaskStatus.NEW);
        history.addTask(t1);
        history.addTask(t2);
        history.remove(1);
        assertEquals(1, history.getHistory().size());
        assertEquals(t2, history.getHistory().get(0));
    }

    @Test
    void removeFromMiddle() {
        Task t1 = new Task(1, "1", "", TaskStatus.NEW);
        Task t2 = new Task(2, "2", "", TaskStatus.NEW);
        Task t3 = new Task(3, "3", "", TaskStatus.NEW);
        history.addTask(t1);
        history.addTask(t2);
        history.addTask(t3);
        history.remove(2);
        assertEquals(2, history.getHistory().size());
        assertFalse(history.getHistory().contains(t2));
    }

    @Test
    void removeFromEnd() {
        Task t1 = new Task(1, "1", "", TaskStatus.NEW);
        Task t2 = new Task(2, "2", "", TaskStatus.NEW);
        history.addTask(t1);
        history.addTask(t2);
        history.remove(2);
        assertEquals(1, history.getHistory().size());
        assertEquals(t1, history.getHistory().get(0));
    }
}
