package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;

    private final Map<Integer, Node> history = new HashMap<>();


    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        int id = task.getId();
        if (history.containsKey(id)) {
            removeNode(history.get(id));
        }
        linkLast(task);
        history.put(id, tail);
    }

    @Override
    public void remove(int id) {
        if (!history.containsKey(id)) return;
        removeNode(history.get(id));
        history.remove(id);
    }

    private void linkLast(Task task) {
        Node newNode = new Node(tail, task, null);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    private void removeNode(Node node) {
        if (node == head && node == tail) {
            head = tail = null;
            return;
        }
        if (node == head) {
            head = head.next;
            head.prev = null;
            return;
        }
        if (node == tail) {
            tail = tail.prev;
            tail.next = null;
            return;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    final class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }
}
