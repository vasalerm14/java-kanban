package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, epic.getId());
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Subtask", subtasks.get(0).getName());
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, epic.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask);
        int id = subtask.getId();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/" + id)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask received = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Subtask", received.getName());
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/999")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask);
        int id = subtask.getId();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/" + id)).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    public void testAddSubtaskWithWrongEpicId() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, 999);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}