# Async File Processor

A Spring Boot service for asynchronous CSV file processing using multithreading.

## What it does

Upload a CSV file — the service processes rows in parallel across multiple threads and saves results to PostgreSQL. You can track processing progress in real time.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Async + CompletableFuture
- PostgreSQL
- Spring Data JPA / Hibernate
- OpenCSV
- Lombok
- Maven

## How multithreading works

1. Client uploads a CSV file via `POST /api/files/upload`
2. Main thread creates a Job in DB (status = PENDING) and immediately returns `jobId`
3. File processing runs in background using `@Async` and a thread pool (4 core, 8 max)
4. Rows are split into batches of 10 — each batch processed in a separate thread simultaneously
5. `AtomicInteger` tracks progress safely across threads
6. Client polls `GET /api/files/status/{jobId}` to check progress

## API

### Upload file
POST /api/files/upload
Content-Type: multipart/form-data
file: test.csv

Response:
```json
{
  "jobId": 1,
  "message": "File uploaded successfully"
}
```

### Check status
GET /api/files/status/{jobId}

Response:
```json
{
  "jobId": 1,
  "status": "COMPLETED",
  "totalLines": 5,
  "processedLines": 5
}
```

## CSV format
firstName,lastName,email
John,Doe,john@example.com
Jane,Smith,jane@example.com

## Setup

**1. Clone the repository**
```bash
git clone https://github.com/sanzhidev/async-file-processor.git
cd async-file-processor
```

**2. Create PostgreSQL database**
```sql
CREATE DATABASE fileprocessing;
```

**3. Configure `application.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fileprocessing
    username: postgres
    password: your_password
```

**4. Run**
```bash
mvn spring-boot:run
```

## Test with curl

```bash
# Upload file
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.csv"

# Check status
curl http://localhost:8080/api/files/status/1