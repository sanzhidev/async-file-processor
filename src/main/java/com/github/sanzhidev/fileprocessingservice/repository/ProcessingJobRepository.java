package com.github.sanzhidev.fileprocessingservice.repository;

import com.github.sanzhidev.fileprocessingservice.model.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
}
