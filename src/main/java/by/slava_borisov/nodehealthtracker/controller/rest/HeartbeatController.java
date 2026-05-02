package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;
import by.slava_borisov.nodehealthtracker.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeat")
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    @GetMapping("/{token}")
    public HeartbeatResponse acceptHeartbeat(@PathVariable String token) {
        return heartbeatService.acceptHeartbeat(token);
    }
}
