package com.github.sanzhidev.fileprocessingservice.controller;

import com.github.sanzhidev.fileprocessingservice.dto.JobStatusResponse;
import com.github.sanzhidev.fileprocessingservice.dto.UploadResponse;
import com.github.sanzhidev.fileprocessingservice.model.ProcessingJob;
import com.github.sanzhidev.fileprocessingservice.service.FileProcessingService;
import com.github.sanzhidev.fileprocessingservice.service.JobTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileProcessingService fileProcessingService; // сервис обработки файла
    private final JobTrackerService jobTrackerService;    // сервис статусов задачи


    // POST /api/files/upload
    // принимает CSV файл, создаёт задачу в БД, запускает обработку в фоне
    // сразу возвращает jobId — клиент использует его чтобы проверять прогресс
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {

        // проверяем что файл не пусто
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new UploadResponse(null, "File is empty"));
        }
        // считаем количество строк чтобы знать totalLines для прогресса
        // -1 потому что первая строка — заголовок CSV
        int totalLines = countLines(file) - 1;


        // создаём задачу в БД со статусом PENDING
        ProcessingJob processingJob = jobTrackerService.createJob(totalLines);

        // запускаем обработку асинхронно — этот метод сразу вернёт управление
        // файл будет обрабатываться в фоне в отдельном потоке
        fileProcessingService.processFile(file, processingJob.getId());

        log.info("Processing file {} of {} lines", file.getOriginalFilename(), totalLines);

        // сразу отвечаем клиенту — не ждём пока файл обработается
        return ResponseEntity.ok(new UploadResponse(processingJob.getId(), "File uploaded successfuly"));
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatusResponse>  getStatus(
            @PathVariable Long jobId) {


        ProcessingJob job = jobTrackerService.findJobId(jobId);

        JobStatusResponse response = new JobStatusResponse(
                job.getId(),
                job.getStatus(),
                job.getTotalLines(),
                job.getProcessedLines()
        );
        return ResponseEntity.ok(response);

    }

    private int countLines(MultipartFile file) {
        try(java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream()))) {
            return (int) reader.lines().count();
        } catch (Exception e) {
            log.error ("Failed to count lines: {}", e.getMessage());
            return 0;
        }
    }


}


