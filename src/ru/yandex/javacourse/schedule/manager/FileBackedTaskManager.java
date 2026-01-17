package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.exceptions.ManageBackupException;
import ru.yandex.javacourse.schedule.tasks.*;
import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;


    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    private void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic,startTime,duration\n");
            for (Task task : super.getTasks()) {
                writer.write(task.toCsvString() + "\n");
            }
            for (Epic epic : super.getEpics()) {
                writer.write(epic.toCsvString() + "\n");
            }
            for (Subtask subtask : super.getSubtasks()) {
                writer.write(subtask.toCsvString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла " + file.getName(), e);
        }
    }

    @Override
    protected int addWithId(Task task) {
        int id = super.addWithId(task);
        save();
        return id;
    }

    @Override
    protected int addWithId(Epic epic) {
        int id = super.addWithId(epic);
        save();
        return id;
    }

    @Override
    protected int addWithId(Subtask subtask) {
        int id = super.addWithId(subtask);
        save();
        return id;
    }

    private void addWithoutSaving(Task task) {
        super.addWithId(task);
    }

    private void addWithoutSaving(Epic epic) {
        super.addWithId(epic);
    }

    private void addWithoutSaving(Subtask subtask) {
        super.addWithId(subtask);
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineArr = line.split(",");
                LocalDateTime startTime = null;
                Duration duration = null;
                if (lineArr.length > 6 && !lineArr[6].isEmpty()) {
                    startTime = LocalDateTime.parse(lineArr[6]);
                }
                if (lineArr.length > 7 && !lineArr[7].isEmpty()) {
                    duration = Duration.ofMinutes(Long.parseLong(lineArr[7]));
                }
                if ("TASK".equals(lineArr[1])) {
                    Task task = new Task(
                            Integer.parseInt(lineArr[0]),
                            lineArr[2],
                            lineArr[4],
                            TaskStatus.valueOf(lineArr[3])
                    );
                    task.setStartTime(startTime);
                    task.setDuration(duration);
                    manager.addWithoutSaving(task);
                } else if ("EPIC".equals(lineArr[1])) {
                    Epic epic = new Epic(
                            Integer.parseInt(lineArr[0]),
                            lineArr[2],
                            lineArr[4]
                    );
                    manager.addWithoutSaving(epic);
                } else if ("SUBTASK".equals(lineArr[1])) {
                    Subtask subtask = new Subtask(
                            Integer.parseInt(lineArr[0]),
                            lineArr[2],
                            lineArr[4],
                            TaskStatus.valueOf(lineArr[3]),
                            Integer.parseInt(lineArr[5])
                    );
                    subtask.setStartTime(startTime);
                    subtask.setDuration(duration);
                    manager.addWithoutSaving(subtask);
                }
            }
        } catch (IOException e) {
            throw new ManageBackupException("Ошибка востановления из файла: " + file.getName(), e);
        }
        return manager;
    }

}
