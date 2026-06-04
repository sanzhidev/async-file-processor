package com.github.sanzhidev.fileprocessingservice.repository;

import com.github.sanzhidev.fileprocessingservice.model.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
}
