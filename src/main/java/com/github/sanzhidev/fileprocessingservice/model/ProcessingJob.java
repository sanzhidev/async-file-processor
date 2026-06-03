package com.github.sanzhidev.fileprocessingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
public class ProcessingJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private int totalLines;
    private int processedLines;

    private LocalDateTime createdAt;
    private LocalDateTime finshedAt;

    public enum JobStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public ProcessingJob(int totalLines){
        this.totalLines = totalLines;
        this.status = JobStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }



}
