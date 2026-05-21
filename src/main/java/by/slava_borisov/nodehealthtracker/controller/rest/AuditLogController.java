package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.audit.AuditLogResponse;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/my")
    public List<AuditLogResponse> getCurrentUserAuditLogs() {
        return auditLogService.getCurrentUserAuditLogs();
    }
}