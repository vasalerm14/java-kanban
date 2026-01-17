package ru.yandex.javacourse.schedule.tasks;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {

    protected List<Integer> subtaskIds = new ArrayList<>();

    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId == this.id) {
            return;
        }
        if (subtaskIds.contains(subtaskId)) {
            return;
        }
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public void updateStatus(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            status = TaskStatus.NEW;
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        if (allNew) {
            status = TaskStatus.NEW;
        } else if (allDone) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    public void updateTimeFields(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }
        Duration total = Duration.ZERO;
        LocalDateTime start = null;
        LocalDateTime end = null;
        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() == null || subtask.getDuration() == null) {
                continue;
            }
            total = total.plus(subtask.getDuration());
            if (start == null || subtask.getStartTime().isBefore(start)) {
                start = subtask.getStartTime();
            }
            if (end == null || subtask.getEndTime().isAfter(end)) {
                end = subtask.getEndTime();
            }
        }
        duration = total;
        startTime = start;
        endTime = end;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toCsvString() {
        String start = startTime == null ? "" : startTime.toString();
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        return id + ",EPIC," + name + "," + status + "," + description + ",," + start + "," + dur;
    }
}
