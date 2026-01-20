package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.util.*;
import java.util.stream.Collectors;

import ru.yandex.javacourse.schedule.exceptions.AddTaskWithIdException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int generatorId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks =
            new TreeSet<>(
                    Comparator
                            .comparing(Task::getStartTime)
                            .thenComparing(Task::getId)
            );


    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        final int id = ++generatorId;
        task.setId(id);
        if (hasIntersection(task)) {
            throw new IllegalStateException("Задача пересекается по времени с другой задачей");
        }
        tasks.put(id, task);
        addToPrioritized(task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final int id = ++generatorId;
        subtask.setId(id);
        if (hasIntersection(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с другой задачей");
        }
        subtasks.put(id, subtask);
        epic.addSubtaskId(subtask.getId());
        addToPrioritized(subtask);
        updateEpic(epicId);
        return id;
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        removeFromPrioritized(savedTask);
        if (hasIntersection(task)) {
            addToPrioritized(savedTask);
            throw new IllegalStateException("Задача пересекается по времени с другой задачей");
        }
        tasks.put(id, task);
        addToPrioritized(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int id = subtask.getId();
        final int epicId = subtask.getEpicId();
        final Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return;
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        removeFromPrioritized(savedSubtask);
        if (hasIntersection(subtask)) {
            addToPrioritized(savedSubtask);
            throw new IllegalStateException("Подзадача пересекается по времени с другой задачей");
        }
        subtasks.put(id, subtask);
        addToPrioritized(subtask);
        updateEpic(epicId);
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        removeFromPrioritized(task);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.remove(subtaskId);
            removeFromPrioritized(subtask);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        removeFromPrioritized(subtask);
        if (subtask == null) {
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        updateEpic(epic.getId());
    }

    @Override
    public void deleteTasks() {
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpic(epic.getId());
        }
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
    }

    @Override
    public void deleteEpics() {
        prioritizedTasks.removeAll(subtasks.values());
        epics.clear();
        subtasks.clear();
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        epic.updateStatus(epicSubtasks);
        epic.updateTimeFields(epicSubtasks);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setStatus(NEW);
            return;
        }
        TaskStatus status = null;
        for (int id : subs) {
            final Subtask subtask = subtasks.get(id);
            if (status == null) {
                status = subtask.getStatus();
                continue;
            }

            if (status == subtask.getStatus()
                    && status != IN_PROGRESS) {
                continue;
            }
            epic.setStatus(IN_PROGRESS);
            return;
        }
        epic.setStatus(status);
    }

    protected void updateGeneratorId(int id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new AddTaskWithIdException("Задача с ID: " + id + " уже существует");
        }
        if (id > generatorId) {
            generatorId = id;
        }
    }

    protected int addWithId(Task task) {
        updateGeneratorId(task.getId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task.getId();
    }

    protected int addWithId(Epic epic) {
        updateGeneratorId(epic.getId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    protected int addWithId(Subtask subtask) {
        updateGeneratorId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpic(epic.getId());
        }
        return subtask.getId();
    }

    private boolean isTasksIntersect(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null) {
            return false;
        }
        if (task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }

        return task1.getStartTime().isBefore(task2.getEndTime())
                && task2.getStartTime().isBefore(task1.getEndTime());
    }

    private boolean hasIntersection(Task task) {
        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .anyMatch(existingTask -> isTasksIntersect(task, existingTask));
    }
}
