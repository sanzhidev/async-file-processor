package com.github.sanzhidev.fileprocessingservice.service;

import com.github.sanzhidev.fileprocessingservice.model.ProcessingJob;
import com.github.sanzhidev.fileprocessingservice.model.UserRecord;
import com.github.sanzhidev.fileprocessingservice.repository.UserRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final UserRecordRepository userRecordRepository;
    private final JobTrackerService jobTrackerService;

    @org.springframework.beans.factory.annotation.Qualifier("fileProcessingExecutor")
    private final java.util.concurrent.Executor executor;


    @Async("fileProcessingExecutor")
    public CompletableFuture<Void> processFile(MultipartFile file, Long jobId) {
        try {
            log.info("Job id={} started in thread: {}", jobId, Thread.currentThread().getName());
            jobTrackerService.markInProgress(jobId);

            List<String> lines = readLines(file);
            int total = lines.size();
            AtomicInteger processedCount = new AtomicInteger(0);
            ProcessingJob job = jobTrackerService.findJobId(jobId);
            List<List<String>> batches = splitIntoBatches(lines, 10);

            List<CompletableFuture<Void>> futures = batches.stream()
                    .map(batch -> CompletableFuture.runAsync(() -> {
                        List<UserRecord> records = new ArrayList<>();
                        for (String line : batch) {
                            UserRecord record = parseLine(line, job);
                            if (record != null) records.add(record);
                        }
                        userRecordRepository.saveAll(records);
                        int processed = processedCount.addAndGet(records.size());
                        jobTrackerService.updateProgress(jobId, processed);
                        log.info("Job id={} thread={} processed {}/{} lines",
                                jobId, Thread.currentThread().getName(), processed, total);
                    }, executor))
                    .toList();


            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            jobTrackerService.markComplete(jobId, processedCount.get());

        } catch (Exception e) {
            log.error("Job id={} failed: {}", jobId, e.getMessage());
            jobTrackerService.markFailed(jobId);
        }

        return CompletableFuture.completedFuture(null);  // возвращаем завершённый future
    }


    // читает все строки из загруженного файла, пропускает заголовок (первую строку)
    private List<String> readLines(MultipartFile file) throws Exception {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;  // первая строка — заголовок CSV, пропускаем
                    continue;
                }
                if (!line.isBlank()) {
                    lines.add(line);   // добавляем только непустые строки
                }
            }
        }
        return lines;
    }

    private UserRecord parseLine(String line, ProcessingJob job) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 3) return null;
          return new UserRecord(parts[0].trim(), parts[1].trim(), parts[2].trim(), job);
        } catch (Exception e) {
            log.warn("Failed to parse line: {}", line);
            return null;
        }
    }

    // делит большой список на маленькие части (батчи) по batchSize элементов
    // например 100 строк с batchSize=10 → 10 батчей по 10 строк
    private List<List<String>> splitIntoBatches(List<String> lines, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += batchSize) {
            batches.add(lines.subList(i, Math.min(i + batchSize, lines.size())));
        }
        return batches;
    }
}
