package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.service.NetworkNodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NetworkNodeController {

    private final NetworkNodeService networkNodeService;

    @PostMapping
    public NodeResponse createNode(@Valid @RequestBody NodeCreateRequest request) {
        return networkNodeService.createNode(request);
    }

    @PutMapping("/{nodeId}")
    public NodeResponse updateNode(
            @PathVariable Long nodeId,
            @Valid @RequestBody NodeUpdateRequest request
    ) {
        return networkNodeService.updateNode(nodeId, request);
    }

    @DeleteMapping("/{nodeId}")
    public void deleteNode(@PathVariable Long nodeId) {
        networkNodeService.deleteNode(nodeId);
    }

    @GetMapping("/{nodeId}")
    public NodeResponse getNodeById(@PathVariable Long nodeId) {
        return networkNodeService.getNodeById(nodeId);
    }

    @GetMapping("/my")
    public List<NodeResponse> getCurrentUserNodes() {
        return networkNodeService.getCurrentUserNodes();
    }
}