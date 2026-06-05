package com.github.sanzhidev.fileprocessingservice.service;

import com.github.sanzhidev.fileprocessingservice.model.ProcessingJob;
import com.github.sanzhidev.fileprocessingservice.repository.ProcessingJobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j   // Lombok: создаёт логгер — можно писать log.info(), log.error()
@Service
@RequiredArgsConstructor
public class JobTrackerService {

    private final ProcessingJobRepository jobRepository;


    // создаёт новую задачу в БД со статусом PENDING
    // @Transactional = все операции с БД внутри метода выполняются как одна транзакция
    @Transactional
    public ProcessingJob createJob(int totalLines) {
        ProcessingJob job = new ProcessingJob(totalLines);   // статус = PENDING, createdAt = now
        ProcessingJob saved = jobRepository.save(job); // сохраняем в БД, получаем id
        log.info("Created job with id {}", saved.getId(),totalLines);
        return saved;
    }

    // обновляет статус задачи на IN_PROGRESS когда начинается обработка
    @Transactional
    public void markInProgress(Long jobId) {
        ProcessingJob job = findJobById(jobId);
        job.setStatus(ProcessingJob.JobStatus.IN_PROGRESS);
        jobRepository.save(job);
        log.info("Job id={} marked as IN_PROGRESS", jobId);
    }

    // обновляет счётчик обработанных строк в реальном времени
    // клиент будет опрашивать GET /status/{jobId} и видеть прогресс
    @Transactional
    public void updateProgress(Long jobId,int processedLines) {
        ProcessingJob job = findJobById(jobId);
        job.setProcessedLines(processedLines);
        jobRepository.save(job);
    }

    // завершает задачу — ставит статус COMPLETED и время окончания
    @Transactional
    public void markComplete(Long jobId,int totalProccedLines) {
        ProcessingJob job = findJobById(jobId);
        job.setStatus(ProcessingJob.JobStatus.COMPLETED);
        job.setProcessedLines(totalProccedLines);
        job.setFinishedAt(LocalDateTime.now()); // фиксируем время завершения
        jobRepository.save(job);
        log.info("Job id={} COMPLETED, processed {} lines", jobId, totalProccedLines);
    }

    // помечает задачу как провалившуюся если произошла ошибка
    @Transactional
    public void markFailed(Long jobId) {
        ProcessingJob job = findJobById(jobId);
        job.setStatus(ProcessingJob.JobStatus.FAILED);
        job.setFinishedAt(LocalDateTime.now());
        jobRepository.save(job);
        log.error("Job id={} FAILED", jobId);
    }

    public ProcessingJob findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found:"+jobId));
    }
}