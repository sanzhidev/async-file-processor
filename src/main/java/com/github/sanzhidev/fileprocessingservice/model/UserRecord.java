package com.github.sanzhidev.fileprocessingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_records")
@Data
@NoArgsConstructor

public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "job_id")
    private ProcessingJob job;

    public UserRecord(String firstName,String lastName,String email, ProcessingJob job){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.job = job;
    }
}
