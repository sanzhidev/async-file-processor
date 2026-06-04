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

    private final FileProcessingService fileProcessingService;
    private final JobTrackerService jobTrackerService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new UploadResponse(null, "File is empty"));
        }

        // копируем файл в байты — чтобы читать только один раз
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new UploadResponse(null, "Failed to read file"));
        }

        // считаем строки из байтов, -1 за заголовок
        int totalLines = (int) new String(fileBytes).lines().count() - 1;

        ProcessingJob job = jobTrackerService.createJob(totalLines);

        log.info("Processing file {} of {} lines", file.getOriginalFilename(), totalLines);

        // передаём байты а не MultipartFile
        fileProcessingService.processFile(fileBytes, job.getId());

        return ResponseEntity.ok(new UploadResponse(job.getId(), "File uploaded successfuly"));
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatusResponse> getStatus(
            @PathVariable Long jobId) {

        ProcessingJob job = jobTrackerService.findJobById(jobId); // исправлено

        JobStatusResponse response = new JobStatusResponse(
                job.getId(),
                job.getStatus(),
                job.getTotalLines(),
                job.getProcessedLines()
        );
        return ResponseEntity.ok(response);
    }
}


