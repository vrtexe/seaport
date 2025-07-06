package mk.ukim.finki.dnick.hosting.infra.config

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.core.task.support.TaskExecutorAdapter
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executors


@EnableAsync
@Configuration
class AsyncConfiguration {

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    fun asyncTaskExecutor(): AsyncTaskExecutor {
        return TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor())
    }
}