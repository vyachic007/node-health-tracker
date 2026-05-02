package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkServiceMapper;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NetworkServiceService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NetworkServiceServiceImpl implements NetworkServiceService {

    private final NetworkServiceRepository networkServiceRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceMapper networkServiceMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public ServiceResponse createService(ServiceCreateRequest request) {
        NetworkNode node = findNodeById(request.nodeId());
        validateNodeOwner(node);

        NetworkService networkService = networkServiceMapper.toEntity(request);
        networkService.setNode(node);
        networkService.setIsEnabled(true);
        networkService.setCreatedAt(LocalDateTime.now());
        networkService.setUpdatedAt(LocalDateTime.now());

        if (networkService.getCheckType() == CheckType.HEARTBEAT) {
            networkService.setHeartbeatToken(generateHeartbeatToken());
        }

        NetworkService savedService = networkServiceRepository.save(networkService);

        return networkServiceMapper.toServiceResponse(savedService);
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long serviceId, ServiceUpdateRequest request) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkServiceMapper.updateEntityFromDto(request, networkService);
        networkService.setUpdatedAt(LocalDateTime.now());

        if (networkService.getCheckType() == CheckType.HEARTBEAT
                && networkService.getHeartbeatToken() == null) {
            networkService.setHeartbeatToken(generateHeartbeatToken());
        }

        if (networkService.getCheckType() != CheckType.HEARTBEAT) {
            networkService.setHeartbeatToken(null);
            networkService.setLastHeartbeatAt(null);
        }

        NetworkService savedService = networkServiceRepository.save(networkService);

        return networkServiceMapper.toServiceResponse(savedService);
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkServiceRepository.delete(networkService);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        return networkServiceMapper.toServiceResponse(networkService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByNodeId(Long nodeId) {
        NetworkNode node = findNodeById(nodeId);
        validateNodeOwner(node);

        return networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(nodeId)
                .stream()
                .map(networkServiceMapper::toServiceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getCurrentUserServices() {
        User currentUser = currentUserService.getCurrentUser();

        return networkServiceRepository.findAllByNodeOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(networkServiceMapper::toServiceResponse)
                .toList();
    }

    @Override
    @Transactional
    public ServiceResponse enableService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkService.setIsEnabled(true);
        networkService.setUpdatedAt(LocalDateTime.now());

        NetworkService savedService = networkServiceRepository.save(networkService);

        return networkServiceMapper.toServiceResponse(savedService);
    }

    @Override
    @Transactional
    public ServiceResponse disableService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkService.setIsEnabled(false);
        networkService.setUpdatedAt(LocalDateTime.now());

        NetworkService savedService = networkServiceRepository.save(networkService);

        return networkServiceMapper.toServiceResponse(savedService);
    }

    private NetworkService findServiceById(Long serviceId) {
        return networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_SERVICE_NOT_FOUND));
    }

    private NetworkNode findNodeById(Long nodeId) {
        return networkNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_NODE_NOT_FOUND));
    }

    private void validateServiceOwner(NetworkService networkService) {
        validateNodeOwner(networkService.getNode());
    }

    private void validateNodeOwner(NetworkNode networkNode) {
        User currentUser = currentUserService.getCurrentUser();

        if (!networkNode.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.NETWORK_NODE_ACCESS_DENIED);
        }
    }

    private String generateHeartbeatToken() {
        return UUID.randomUUID().toString();
    }
}