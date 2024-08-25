package com.example.githubapi.controller;

import com.example.githubapi.exception.RateExceededException;
import com.example.githubapi.model.RepositoryInfo;
import com.example.githubapi.service.GithubService;
import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.model.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GithubController {
    private final GithubService githubService;
    private static final Logger log = LoggerFactory.getLogger(GithubController.class);

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repos/{username}")
    public ResponseEntity<?> getUserRepositories(@PathVariable String username,
                                                 @RequestHeader("Accept") String acceptHeader) {
        if (!"application/json".equals(acceptHeader)) {
            return ResponseEntity.badRequest().body("Accept header must be application/json");
        }

        try {
            List<RepositoryInfo> repositories = githubService.getUserRepositories(username);
            return ResponseEntity.ok(repositories);
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", username);
            ErrorResponse errorResponse = new ErrorResponse(404, e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (RateExceededException e) {
            log.warn("Rate limit exceeded: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(403, e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (RuntimeException e) {
            log.error("Error fetching repositories: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(500, "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}