package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest
        extends TaskManagerTest<FileBackedTaskManager> {

    private File file;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            file = File.createTempFile("kanban", ".csv");
            return new FileBackedTaskManager(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldLoadEmptyFile() {
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(file));
    }

    @Test
    void shouldSaveAndLoadTask() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);
        FileBackedTaskManager loaded =
                FileBackedTaskManager.loadFromFile(file);
        assertEquals(task, loaded.getTask(task.getId()));
    }
}
