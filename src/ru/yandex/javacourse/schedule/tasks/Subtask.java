package ru.yandex.javacourse.schedule.tasks;

public class Subtask extends Task {

    protected final int epicId;

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toCsvString() {
        String start = startTime == null ? "" : startTime.toString();
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        return id + ",SUBTASK," + name + "," + status + "," + description + "," + epicId + "," + start + "," + dur;
    }
}
