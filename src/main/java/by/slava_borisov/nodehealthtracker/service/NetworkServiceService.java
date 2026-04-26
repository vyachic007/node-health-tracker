package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;

import java.util.List;

public interface NetworkServiceService {

    ServiceResponse createService(ServiceCreateRequest request);

    ServiceResponse updateService(Long serviceId, ServiceUpdateRequest request);

    void deleteService(Long serviceId);

    ServiceResponse getServiceById(Long serviceId);

    List<ServiceResponse> getServicesByNodeId(Long nodeId);

    List<ServiceResponse> getCurrentUserServices();

    ServiceResponse enableService(Long serviceId);

    ServiceResponse disableService(Long serviceId);
}