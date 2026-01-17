package ru.yandex.javacourse.schedule.manager;

public class InMemoryTaskManagerTest
        extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}
