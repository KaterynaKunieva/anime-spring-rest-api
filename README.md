# Anime Management REST API

A Spring Boot application for managing anime and authors data with PostgreSQL database and REST API.

## Entities

The application contains two main entities:

1. **Anime** - Represents an anime with title, release year, score, and associated author
2. **Author** - Represents an author with a unique name and associated anime list

## Tech Stack

- Spring Boot 3.5.8
- Java 21
- PostgreSQL 16.2
- Liquibase (migrations)
- JPA/Hibernate
- Maven

## Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.6+

## Running the Application

1. Start PostgreSQL:
```bash
docker-compose -f docker-compose-infra.yml up -d
```
**Important Notes:**
- Docker Desktop must be running before executing docker-compose commands
- The application uses port 5432 by default for database connections
- If port 5432 is already in use, modify the port in the following files:
    - `docker-compose-infra.yml`
    - `src/main/resources/application-local.properties`
2. Install Dependencies using Maven:
```bash
mvn clean install
```
3. Run Tests:
```bash
mvn test
```
3. Run the application:
```bash
mvn spring-boot:run
```
The application will start on [http://localhost:8080](http://localhost:8080)

## API Documentation

Once the application is running, access the Swagger UI documentation at:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

The Swagger interface provides interactive documentation for all available endpoints.

## Database

- **User**: user
- **Password**: 123456789
- **Database**: anime
- **Port**: 5432

## Sample Data

Sample anime data is available in the `data/anime.json` file.

## API Endpoints

### Authors

- `GET /api/author` - Get all authors
- `POST /api/author` - Create a new author
- `PUT /api/author/{id}` - Update an author
- `DELETE /api/author/{id}` - Delete an author

### Anime

- `POST /api/anime` - Create a new anime
- `GET /api/anime/{id}` - Get anime by id
- `PUT /api/anime/{id}` - Update anime
- `DELETE /api/anime/{id}` - Delete anime
- `POST /api/anime/_list` - Get paginated anime list with filters
- `POST /api/anime/_report` - Generate CSV report
- `POST /api/anime/upload` - Import anime from JSON file


## Project Structure

- **Controllers**: REST endpoints for managing authors and anime
- **Services**: Business logic for anime and author operations
- **Data Models**: Entity classes (Author, Anime)
- **Repositories**: JPA data access layer
- **DTOs**: Data transfer objects for request and response handling
- **Exception Handling**: Global exception handling
- **Resources**: Application configuration
