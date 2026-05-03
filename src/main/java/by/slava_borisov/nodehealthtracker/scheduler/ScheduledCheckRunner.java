package by.slava_borisov.nodehealthtracker.scheduler;

import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledCheckRunner {

    private final CheckExecutionService checkExecutionService;

    @Value("${app.monitoring.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedDelayString = "${app.monitoring.scheduler.fixed-delay-ms:60000}")
    public void runDueChecks() {
        if (!schedulerEnabled) {
            log.debug("Планировщик мониторинга отключен");
            return;
        }

        log.info("Запуск плановой проверки сервисов по индивидуальным интервалам");

        try {
            List<CheckResultResponse> results = checkExecutionService.runDueChecks();

            log.info(
                    "Плановая проверка завершена: checkedServices={}",
                    results.size()
            );
        } catch (Exception exception) {
            log.error("Ошибка во время плановой проверки сервисов", exception);
        }
    }
}