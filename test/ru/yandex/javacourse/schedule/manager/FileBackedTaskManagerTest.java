package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.AddTaskWithIdException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest {
    private File tempFile;

    @BeforeEach
    void initTempFile() throws IOException {
        tempFile = File.createTempFile("kanban-test-", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("id,type,name,status,description,epic\n");
            writer.write("1,TASK,Task1,NEW,Description task1,\n");
            writer.write("2,EPIC,Epic2,DONE,Description epic2,\n");
            writer.write("3,SUBTASK,Sub Task2,DONE,Description sub task3,2\n");
        }
        tempFile.deleteOnExit();
    }

    @Test
    @DisplayName("Проверка что пустой файл загружается корректно")
    void shouldLoadEmptyFileCorrectly() throws IOException {
        File emptyFile = File.createTempFile("kanban-empty-", ".csv");
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(emptyFile);
        assertEquals(0, loadManager.getTasks().size(), "Tasks not empty");
        assertEquals(0, loadManager.getEpics().size(), "Epics not empty");
        assertEquals(0, loadManager.getSubtasks().size(), "Subtasks not empty");

        emptyFile.deleteOnExit();
    }

    @Test
    @DisplayName("Проверка на корректную загрузку не пустого файла")
    void TestLoadFromFile() {
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadManager.getTasks().size(), "Wrong task count");
        assertEquals(1, loadManager.getEpics().size(), "Wrong epic count");
        assertEquals(1, loadManager.getSubtasks().size(), "Wrong subtask count");
    }

    @Test
    @DisplayName("Проверка на корреткную загрузку Тасков")
    void checkTaskBackUp() {
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        assertEquals(task, loadManager.getTask(1), "Wrong task backup");
    }

    @Test
    @DisplayName("Проверка на корректную загрузку Эпиков и Сабтасков")
    void checkEpicAndSubtaskBackup() {
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic epic = new Epic(2, "Epic2", "Description epic2");
        Subtask subtask = new Subtask(3, "Sub Task2", "Description sub task3", TaskStatus.DONE, 2);
        assertEquals(epic, loadManager.getEpic(2), "Wrong epic backup");
        assertEquals(subtask, loadManager.getSubtask(3), "Wrong subtask backup");
    }

    @Test
    @DisplayName("Проверка на сохранение нового таска")
    void checkSaveNewTask() throws IOException {
        File emptyFile = File.createTempFile("kanban-empty-", ".csv");
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(emptyFile);
        Task task = new Task(2, "Task1", "Description task1", TaskStatus.NEW);
        loadManager.addWithId(task);
        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(emptyFile);
        Task loadedTask = reloaded.getTask(2);
        assertEquals(task, loadedTask, "Wrong task save");
    }

    @Test
    @DisplayName("Проверка на сохранение нового эпика и сабтаска")
    void checkSaveEpicAndSubtask() throws IOException {
        File emptyFile = File.createTempFile("kanban-empty-", ".csv");
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(emptyFile);
        Epic epic = new Epic(2, "Epic2", "Description epic2");
        Subtask subtask = new Subtask(3, "Sub Task2", "Description sub task3", TaskStatus.DONE, 1);
        loadManager.addWithId(epic);
        loadManager.addWithId(subtask);
        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(emptyFile);
        Epic loadedEpic = reloaded.getEpic(2);
        Subtask loadedSubtask = reloaded.getSubtask(3);
        assertEquals(epic, loadedEpic, "Wrong epic save");
        assertEquals(subtask, loadedSubtask, "Wrong subtask save");
    }

    @Test
    @DisplayName("Проверка на защиту от сохранения двух задач с одним id")
    void checkSaveWithSameID() {
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        assertThrows(AddTaskWithIdException.class, () -> loadManager.addWithId(task));
    }
}
