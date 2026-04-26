package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;

import java.util.List;

public interface NetworkNodeService {

    NodeResponse createNode(NodeCreateRequest request);

    NodeResponse updateNode(Long nodeId, NodeUpdateRequest request);

    void deleteNode(Long nodeId);

    NodeResponse getNodeById(Long nodeId);

    List<NodeResponse> getCurrentUserNodes();
}