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

public class HttpTaskServerEpicsTest {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size());
        assertEquals("Epic", epics.get(0).getName());
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        manager.addNewEpic(new Epic("Epic", "Epic desc"));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(1, epics.length);
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        int id = epic.getId();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/" + id)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic received = gson.fromJson(response.body(), Epic.class);
        assertEquals("Epic", received.getName());
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/999")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        int id = epic.getId();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/" + id)).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        manager.addNewEpic(epic);
        manager.addNewSubtask(new Subtask("Subtask", "Subtask desc", TaskStatus.NEW, epic.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
    }
}
