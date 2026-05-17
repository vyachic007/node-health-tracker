package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.dashboard.DashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CurrentUserService currentUserService;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final IncidentRepository incidentRepository;
    private final CheckResultRepository checkResultRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getCurrentUserSummary() {
        User currentUser = currentUserService.getCurrentUser();
        Long ownerId = currentUser.getId();

        long totalNodes = networkNodeRepository.countByOwnerId(ownerId);

        long totalServices = networkServiceRepository.countByNodeOwnerId(ownerId);
        long enabledServices = networkServiceRepository.countByNodeOwnerIdAndIsEnabledTrue(ownerId);
        long disabledServices = networkServiceRepository.countByNodeOwnerIdAndIsEnabledFalse(ownerId);

        long upServices = networkServiceRepository.countCurrentServicesByStatus(
                ownerId,
                ServiceStatus.UP.name()
        );

        long downServices = networkServiceRepository.countCurrentServicesByStatus(
                ownerId,
                ServiceStatus.DOWN.name()
        );

        long openIncidents = incidentRepository.countByServiceNodeOwnerIdAndStatus(
                ownerId,
                IncidentStatus.OPEN
        );

        long resolvedIncidents = incidentRepository.countByServiceNodeOwnerIdAndStatus(
                ownerId,
                IncidentStatus.RESOLVED
        );

        long checksLast24Hours = checkResultRepository.countByServiceNodeOwnerIdAndCheckedAtAfter(
                ownerId,
                LocalDateTime.now().minusHours(24)
        );

        return new DashboardSummaryResponse(
                totalNodes,
                totalServices,
                enabledServices,
                disabledServices,
                upServices,
                downServices,
                openIncidents,
                resolvedIncidents,
                checksLast24Hours
        );
    }
}