# GitHub API Service

This project provides a service to interact with the GitHub API, allowing users to retrieve information about repositories and their branches for a given GitHub username.

## Features

- Fetch non-forked repositories for a given GitHub user
- Retrieve branch information for each repository
- Reactive programming model using Spring WebFlux
- Error handling for user not found scenarios
- Graceful handling of exceeding GitHub API rate limit

## Technologies Used

- Java
- Spring Boot
- Spring WebFlux
- WebClient for making HTTP requests
- Reactor Core for reactive programming

## Project Structure

The main components of the project are:

- `GithubService`: Service class that interacts with the GitHub API
- `RepositoryInfo`: Model class representing repository information
- `BranchInfo`: Model class representing branch information
- `UserNotFoundException`: Custom exception for handling user not found errors

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven or Gradle (for building the project)

### Building the Project

To build the project, run:

```
mvn clean install
```

To run the project, run:

```
mvn spring-boot:run
```


## Usage

The main entry point for using this service is the `/api/repos/{github_username}' endpoint.
Here's an example of how to use it 
```shell
Invoke-WebRequest -Uri https://julias-github-app/api/repos/user -Headers @{"Accept"="application/json"}
```

### !!!Important!!!
Please remember that to use the API effectively, you need to provide it with a **GITHUB ACCESS TOKEN**
It is provided via the environment variable _GITHUB_TOKEN_



## API Endpoints
The service interacts with the GitHub API through the following endpoints:

* `/users/{username}/repos`: To fetch user repositories
* Repository-specific branches URL: To fetch branch information for each repo.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
