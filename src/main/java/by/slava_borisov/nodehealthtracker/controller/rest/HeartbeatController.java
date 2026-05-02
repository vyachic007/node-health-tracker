package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;
import by.slava_borisov.nodehealthtracker.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/heartbeat")
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    @PostMapping("/{token}")
    public HeartbeatResponse acceptHeartbeat(@PathVariable String token) {
        return heartbeatService.acceptHeartbeat(token);
    }
}
