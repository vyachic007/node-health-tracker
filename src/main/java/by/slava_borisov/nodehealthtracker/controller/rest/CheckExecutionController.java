package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checks")
@RequiredArgsConstructor
public class CheckExecutionController {

    private final CheckExecutionService checkExecutionService;

    @PostMapping("/services/{serviceId}/run")
    public CheckResultResponse runCheck(@PathVariable Long serviceId) {
        return checkExecutionService.runCheck(serviceId);
    }

    @PostMapping("/run-enabled")
    public List<CheckResultResponse> runEnabledChecks() {
        return checkExecutionService.runEnabledChecks();
    }

    @GetMapping("/services/{serviceId}/history")
    public List<CheckResultResponse> getCheckHistory(@PathVariable Long serviceId) {
        return checkExecutionService.getCheckHistory(serviceId);
    }
}