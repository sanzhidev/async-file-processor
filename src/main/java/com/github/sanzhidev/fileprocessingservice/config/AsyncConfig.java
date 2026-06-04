package com.github.sanzhidev.fileprocessingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "fileProcessingExecutor") // создаёт бин с именем — по этому имени будем ссылаться в @Async
    public Executor fileProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // минимальное количество потоков которые всегда живут в пуле
        // даже если задач нет — эти 4 потока всё равно существуют
        executor.setCorePoolSize(4);


        // максимальное количество потоков при высокой нагрузке
        // если задач больше чем corePoolSize — создаются новые потоки до этого предела
        executor.setMaxPoolSize(8);

        // очередь ожидания: если все 8 потоков заняты — новые задачи ждут здесь
        // 100 = максимум 100 задач в очереди
        executor.setQueueCapacity(100);

        // префикс имени потока — будет видно в логах: "file-processor-1", "file-processor-2"
        // удобно для отладки чтобы понимать какой поток что делает
        executor.setThreadNamePrefix("file-processor-");

        // инициализирует executor — обязательно вызывать перед использованием
        executor.initialize();

        return executor;


    }
}
