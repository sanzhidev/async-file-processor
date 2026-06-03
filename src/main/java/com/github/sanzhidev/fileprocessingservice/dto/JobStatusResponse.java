package com.github.sanzhidev.fileprocessingservice.dto;

import com.github.sanzhidev.fileprocessingservice.model.ProcessingJob;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class JobStatusResponse {
    private Long JobId;  // id задачи
    private ProcessingJob.JobStatus status; // текущий статус:
    private int totalLines;
    private int processedLines;
}
