package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.service.NetworkServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class NetworkServiceController {

    private final NetworkServiceService networkServiceService;

    @PostMapping
    public ServiceResponse createService(@Valid @RequestBody ServiceCreateRequest request) {
        return networkServiceService.createService(request);
    }

    @PutMapping("/{serviceId}")
    public ServiceResponse updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceUpdateRequest request
    ) {
        return networkServiceService.updateService(serviceId, request);
    }

    @DeleteMapping("/{serviceId}")
    public void deleteService(@PathVariable Long serviceId) {
        networkServiceService.deleteService(serviceId);
    }

    @GetMapping("/{serviceId}")
    public ServiceResponse getServiceById(@PathVariable Long serviceId) {
        return networkServiceService.getServiceById(serviceId);
    }

    @GetMapping("/node/{nodeId}")
    public List<ServiceResponse> getServicesByNodeId(@PathVariable Long nodeId) {
        return networkServiceService.getServicesByNodeId(nodeId);
    }

    @GetMapping("/my")
    public List<ServiceResponse> getCurrentUserServices() {
        return networkServiceService.getCurrentUserServices();
    }

    @PostMapping("/{serviceId}/enable")
    public ServiceResponse enableService(@PathVariable Long serviceId) {
        return networkServiceService.enableService(serviceId);
    }

    @PostMapping("/{serviceId}/disable")
    public ServiceResponse disableService(@PathVariable Long serviceId) {
        return networkServiceService.disableService(serviceId);
    }
}