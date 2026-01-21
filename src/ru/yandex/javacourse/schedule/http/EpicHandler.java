package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;
    private final Gson gson = HttpTaskServer.getGson();

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (path.matches("^/epics$")) {
                handleEpics(exchange, method);
                return;
            }
            if (path.matches("^/epics/\\d+/subtasks$")) {
                handleEpicSubtasks(exchange, method);
                return;
            }
            if (path.matches("^/epics/\\d+$")) {
                handleEpicById(exchange, method);
                return;
            }
            sendNotFound(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleEpics(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            List<Epic> epics = taskManager.getEpics();
            sendOk(exchange, gson.toJson(epics));
            return;
        }
        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Epic epic = gson.fromJson(body, Epic.class);
            taskManager.addNewEpic(epic);
            sendCreated(exchange, "Epic added");
            return;
        }
        sendNotFound(exchange);
    }

    private void handleEpicById(HttpExchange exchange, String method) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().replaceAll("\\D+", ""));
        if ("GET".equals(method)) {
            Epic epic = taskManager.getEpic(id);
            if (epic == null) {
                sendNotFound(exchange);
                return;
            }
            sendOk(exchange, gson.toJson(epic));
            return;
        }
        if ("DELETE".equals(method)) {
            taskManager.deleteEpic(id);
            sendOk(exchange, "Epic deleted");
            return;
        }
        sendNotFound(exchange);
    }

    private void handleEpicSubtasks(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            sendNotFound(exchange);
            return;
        }
        int id = Integer.parseInt(exchange.getRequestURI().getPath().replaceAll("\\D+", ""));
        List<Subtask> subtasks = taskManager.getEpicSubtasks(id);
        if (subtasks == null) {
            sendNotFound(exchange);
            return;
        }
        sendOk(exchange, gson.toJson(subtasks));
    }
}
