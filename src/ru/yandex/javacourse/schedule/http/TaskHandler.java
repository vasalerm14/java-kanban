package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson = HttpTaskServer.getGson();

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (path.matches("^/tasks$")) {
                handleTasks(exchange, method);
                return;
            }
            if (path.matches("^/tasks/\\d+$")) {
                handleTaskById(exchange, method);
                return;
            }
            sendNotFound(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleTasks(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            List<Task> tasks = taskManager.getTasks();
            sendOk(exchange, gson.toJson(tasks));
            return;
        }
        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Task task = gson.fromJson(body, Task.class);
            taskManager.addNewTask(task);
            sendCreated(exchange, "Task added");
            return;
        }
        sendNotFound(exchange);
    }

    private void handleTaskById(HttpExchange exchange, String method) throws IOException {
        String path = exchange.getRequestURI().getPath();
        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        if ("GET".equals(method)) {
            Task task = taskManager.getTask(id);
            if (task == null) {
                sendNotFound(exchange);
                return;
            }
            sendOk(exchange, gson.toJson(task));
            return;
        }
        if ("DELETE".equals(method)) {
            taskManager.deleteTask(id);
            sendOk(exchange, "Task deleted");
            return;
        }
        sendNotFound(exchange);
    }
}
