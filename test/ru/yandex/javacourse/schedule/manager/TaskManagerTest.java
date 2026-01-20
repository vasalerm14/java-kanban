package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void init() {
        manager = createManager();
    }

    @Test
    void shouldAddTask() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size());
        assertEquals(task, manager.getTask(task.getId()));
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);
        Task updated = new Task(task.getId(), "New", "New", TaskStatus.DONE);
        manager.updateTask(updated);
        assertEquals(TaskStatus.DONE, manager.getTask(task.getId()).getStatus());
    }

    @Test
    void epicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Sub1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.addNewSubtask(new Subtask("Sub2", "Desc", TaskStatus.NEW, epic.getId()));
        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Sub1", "Desc", TaskStatus.DONE, epic.getId()));
        manager.addNewSubtask(new Subtask("Sub2", "Desc", TaskStatus.DONE, epic.getId()));
        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusMixedNewDone() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Sub1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.addNewSubtask(new Subtask("Sub2", "Desc", TaskStatus.DONE, epic.getId()));
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        manager.addNewSubtask(new Subtask("Sub", "Desc", TaskStatus.IN_PROGRESS, epic.getId()));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnTasksSortedByStartTime() {
        Task t1 = new Task("T1", "Desc", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));
        Task t2 = new Task("T2", "Desc", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2026, 1, 1, 9, 0));
        t2.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(t1);
        manager.addNewTask(t2);
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2, prioritized.get(0));
        assertEquals(t1, prioritized.get(1));
    }

    @Test
    void shouldThrowExceptionOnIntersection() {
        Task t1 = new Task("T1", "Desc", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        Task t2 = new Task("T2", "Desc", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(t1);
        assertThrows(
                IllegalStateException.class,
                () -> manager.addNewTask(t2)
        );
    }

    @Test
    void shouldReturnEpicSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        Subtask s1 = new Subtask("S1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask s2 = new Subtask("S2", "Desc", TaskStatus.NEW, epic.getId());

        manager.addNewSubtask(s1);
        manager.addNewSubtask(s2);

        List<Subtask> subs = manager.getEpicSubtasks(epic.getId());

        assertEquals(2, subs.size());
        assertTrue(subs.contains(s1));
        assertTrue(subs.contains(s2));
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);

        manager.deleteTask(task.getId());

        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        manager.addNewSubtask(new Subtask("Sub", "Desc", TaskStatus.NEW, epic.getId()));

        manager.deleteEpic(epic.getId());

        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.addNewTask(new Task("T", "D", TaskStatus.NEW));
        manager.deleteTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpics() {
        manager.addNewEpic(new Epic("T", "D"));
        manager.deleteEpics();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Sub", "Desc", TaskStatus.NEW, epic.getId()));
        manager.deleteSubtasks();
        assertTrue(manager.getSubtasks().isEmpty());
    }
}
