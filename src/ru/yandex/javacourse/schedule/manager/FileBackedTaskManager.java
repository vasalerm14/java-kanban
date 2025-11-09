package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.exceptions.ManageBackupException;
import ru.yandex.javacourse.schedule.tasks.*;
import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager{

    private File file;


    public FileBackedTaskManager(File file){
        this.file = file;
    }

    @Override
    public int addNewTask(Task task){
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic){
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task){
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic){
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask){
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id){
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id){
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id){
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks(){
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics(){
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks(){
        super.deleteSubtasks();
        save();
    }

    @Override
    public Integer addNewSubtask(Subtask subtask){
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    private void save(){
        try (Writer writer = new FileWriter(file)) {
            writer.append("id,type,name,status,description,epic\n");

            for(Task task : super.getTasks()){
                writer.append(task.toCsvString() + "\n");
            }

            for(Epic epic : super.getEpics()){
                writer.append(epic.toCsvString() + "\n");
            }

            for(Subtask subtask : super.getSubtasks()){
                writer.append(subtask.toCsvString() + "\n");
            }
        } catch (IOException e){
            throw new ManagerSaveException("Ошибка при сохранении файла " + file.getName(),e);
        }
    }

    private void addWithoutSaving(Task task) {
        super.addNewTask(task);
    }

    private void addWithoutSaving(Epic epic) {
        super.addNewEpic(epic);
    }

    private void addWithoutSaving(Subtask subtask) {
        super.addNewSubtask(subtask);
    }


    public static FileBackedTaskManager loadFromFile(File file){
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            reader.readLine();
            while(reader.ready()){
                String line = reader.readLine();
                String[] lineArr = line.split(",");
                if(lineArr[1].equals("TASK")){
                    int id = Integer.parseInt(lineArr[0]);
                    String name = lineArr[2];
                    TaskStatus status = TaskStatus.valueOf(lineArr[3]);
                    String description = lineArr[4];
                    manager.addWithoutSaving(new Task(id,name,description,status));
                } else if(lineArr[1].equals("EPIC")){
                    int id = Integer.parseInt(lineArr[0]);
                    String name = lineArr[2];
                    String description = lineArr[4];
                    manager.addWithoutSaving(new Epic(id,name,description));
                } else if(lineArr[1].equals("SUBTASK")){
                    int id = Integer.parseInt(lineArr[0]);
                    String name = lineArr[2];
                    TaskStatus status = TaskStatus.valueOf(lineArr[3]);
                    String description = lineArr[4];
                    int epicId = Integer.valueOf(lineArr[5]);
                    manager.addWithoutSaving(new Subtask(id,name,description,status,epicId));
                }
            }
        } catch (IOException e) {
            throw new ManageBackupException("Ошибка востановления из файла: " + file.getName(),e);
        }
        return manager;
    }




}
