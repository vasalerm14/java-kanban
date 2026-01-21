package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;
    private final Gson gson = HttpTaskServer.getGson();

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (path.matches("^/subtasks$")) {
                handleSubtasks(exchange, method);
                return;
            }
            if (path.matches("^/subtasks/\\d+$")) {
                handleSubtaskById(exchange, method);
                return;
            }
            sendNotFound(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleSubtasks(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            sendOk(exchange, gson.toJson(subtasks));
            return;
        }
        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (taskManager.getEpic(subtask.getEpicId()) == null) {
                sendNotFound(exchange);
                return;
            }
            taskManager.addNewSubtask(subtask);
            sendCreated(exchange, "Subtask added");
            return;
        }
        sendNotFound(exchange);
    }

    private void handleSubtaskById(HttpExchange exchange, String method) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().replaceAll("\\D+", ""));
        if ("GET".equals(method)) {
            Subtask subtask = taskManager.getSubtask(id);
            if (subtask == null) {
                sendNotFound(exchange);
                return;
            }
            sendOk(exchange, gson.toJson(subtask));
            return;
        }
        if ("DELETE".equals(method)) {
            taskManager.deleteSubtask(id);
            sendOk(exchange, "Subtask deleted");
            return;
        }
        sendNotFound(exchange);
    }
}
