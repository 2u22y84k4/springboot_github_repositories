package com.example.githubapi;
import com.example.githubapi.service.GithubService;


import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.model.BranchInfo;
import com.example.githubapi.model.RepositoryInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GithubServiceTest {

    public static MockWebServer mockBackEnd;
    private GithubService githubService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        githubService = new GithubService(WebClient.builder().baseUrl(baseUrl));
    }

    @Test
    void getUserRepositories_whenUserDoesNotExist_throwsUserNotFoundException() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(404));

        assertThrows(UserNotFoundException.class, () -> {
            githubService.getUserRepositories("nonexistentuser");
        });
    }

    @Test
    void getUserRepositories_whenOnlyForkedRepos_returnsEmptyList() {
        String reposJson = "[{\"name\":\"repo1\",\"fork\":true,\"owner\":{\"login\":\"testuser\"}}]";

        mockBackEnd.enqueue(new MockResponse()
                .setBody(reposJson)
                .addHeader("Content-Type", "application/json"));

        List<RepositoryInfo> repositories = githubService.getUserRepositories("testuser");

        assertNotNull(repositories);
        assertTrue(repositories.isEmpty());
    }
}