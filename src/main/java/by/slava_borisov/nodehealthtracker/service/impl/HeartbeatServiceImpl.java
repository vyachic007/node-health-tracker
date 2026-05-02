package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.HeartbeatService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HeartbeatServiceImpl implements HeartbeatService {

    private final NetworkServiceRepository networkServiceRepository;

    @Override
    @Transactional
    public HeartbeatResponse acceptHeartbeat(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidOperationException(Messages.HEARTBEAT_TOKEN_REQUIRED);
        }

        NetworkService networkService = networkServiceRepository.findByHeartbeatToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.HEARTBEAT_TOKEN_NOT_FOUND));

        LocalDateTime heartbeatTime = LocalDateTime.now();

        networkService.setLastHeartbeatAt(heartbeatTime);
        networkService.setUpdatedAt(heartbeatTime);

        NetworkService savedService = networkServiceRepository.save(networkService);

        return new HeartbeatResponse(
                savedService.getId(),
                savedService.getName(),
                savedService.getLastHeartbeatAt(),
                Messages.HEARTBEAT_ACCEPTED
        );
    }
}