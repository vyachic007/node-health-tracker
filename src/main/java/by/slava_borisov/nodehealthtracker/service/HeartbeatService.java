package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;

public interface HeartbeatService {

    HeartbeatResponse acceptHeartbeat(String token);
}