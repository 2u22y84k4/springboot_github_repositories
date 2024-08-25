package com.example.githubapi.service;

import com.example.githubapi.exception.RateExceededException;
import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.model.BranchInfo;
import com.example.githubapi.model.RepositoryInfo;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(GithubService.class);
    private static final String GITHUB_API_URL = "https://api.github.com";

    public GithubService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(GITHUB_API_URL).build();
    }

    public List<RepositoryInfo> getUserRepositories(String username) {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.info("Rate Limit Remaining: {}", response.headers().header("X-RateLimit-Remaining"));
                    log.info("Rate Limit Reset: {}", response.headers().header("X-RateLimit-Reset"));
                    return response.bodyToMono(String.class)
                            .flatMap(error -> {
                                if (response.statusCode() == HttpStatus.FORBIDDEN) {
                                    return Mono.error(new RateExceededException("GitHub API access forbidden. Error: " + error));
                                }
                                if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                    return Mono.error(new UserNotFoundException("User not found: " + username));
                                }
                                return Mono.error(new RuntimeException("Client error: " + response.statusCode() + ". Error: " + error));
                            });
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new RuntimeException("Server error: " + response.statusCode() + ". Error: " + error)));
                })
                .bodyToFlux(Map.class)
                .filter(repo -> !(Boolean) repo.get("fork"))
                .map(this::mapToRepositoryInfo)
                .collectList()
                .block();
    }

    private RepositoryInfo mapToRepositoryInfo(Map<String, Object> repo) {
        RepositoryInfo repoInfo = new RepositoryInfo();
        repoInfo.setName((String) repo.get("name"));
        Map<String, Object> owner = (Map<String, Object>) repo.get("owner");
        repoInfo.setOwnerLogin((String) owner.get("login"));
        repoInfo.setBranches(getBranches((String) repo.get("branches_url")));
        return repoInfo;
    }

    private List<BranchInfo> getBranches(String branchesUrl) {
        branchesUrl = branchesUrl.replace("{/branch}", "");
        return webClient.get()
                .uri(branchesUrl)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(this::mapToBranchInfo)
                .collectList()
                .block();
    }

    private BranchInfo mapToBranchInfo(Map<String, Object> branch) {
        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setName((String) branch.get("name"));
        Map<String, Object> commit = (Map<String, Object>) branch.get("commit");
        branchInfo.setLastCommitSha((String) commit.get("sha"));
        return branchInfo;
    }
}