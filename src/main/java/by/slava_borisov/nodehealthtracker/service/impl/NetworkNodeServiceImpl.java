package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkNodeMapper;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NetworkNodeService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NetworkNodeServiceImpl implements NetworkNodeService {

    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkNodeMapper networkNodeMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public NodeResponse createNode(NodeCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        NetworkNode networkNode = networkNodeMapper.toEntity(request);
        networkNode.setOwner(currentUser);
        networkNode.setIsActive(true);
        networkNode.setCreatedAt(LocalDateTime.now());
        networkNode.setUpdatedAt(LocalDateTime.now());

        NetworkNode savedNode = networkNodeRepository.save(networkNode);

        return networkNodeMapper.toNodeResponse(savedNode);
    }

    @Override
    @Transactional
    public NodeResponse updateNode(Long nodeId, NodeUpdateRequest request) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        networkNodeMapper.updateEntityFromDto(request, networkNode);
        networkNode.setUpdatedAt(LocalDateTime.now());

        NetworkNode savedNode = networkNodeRepository.save(networkNode);

        return networkNodeMapper.toNodeResponse(savedNode);
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        networkNodeRepository.delete(networkNode);
    }

    @Override
    @Transactional(readOnly = true)
    public NodeResponse getNodeById(Long nodeId) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        return networkNodeMapper.toNodeResponse(networkNode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeResponse> getCurrentUserNodes() {
        User currentUser = currentUserService.getCurrentUser();

        return networkNodeRepository.findAllByOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(networkNodeMapper::toNodeResponse)
                .toList();
    }

    private NetworkNode findNodeById(Long nodeId) {
        return networkNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_NODE_NOT_FOUND));
    }

    private void validateNodeOwner(NetworkNode networkNode) {
        User currentUser = currentUserService.getCurrentUser();

        if (!networkNode.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.NETWORK_NODE_ACCESS_DENIED);
        }
    }
}