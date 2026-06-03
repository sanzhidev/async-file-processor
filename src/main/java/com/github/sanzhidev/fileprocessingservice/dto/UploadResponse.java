package com.github.sanzhidev.fileprocessingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private Long JobId;  // id созданной задачи — клиент сохранит его чтобы потом проверять статус
    private String message; // сообщение, например "File uploaded successfully"

}
