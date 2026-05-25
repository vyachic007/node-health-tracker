import { apiClient } from '../../../shared/api/apiClient';
import type {
    CheckResult,
    CreateNetworkServiceRequest,
    NetworkService,
    UpdateNetworkServiceRequest,
} from '../model/serviceTypes';

export const servicesApi = {
    async getMyServices(): Promise<NetworkService[]> {
        const response = await apiClient.get<NetworkService[]>('/api/services/my');

        return response.data;
    },

    async getService(serviceId: number): Promise<NetworkService> {
        const response = await apiClient.get<NetworkService>(
            `/api/services/${serviceId}`,
        );

        return response.data;
    },

    async createService(
        payload: CreateNetworkServiceRequest,
    ): Promise<NetworkService> {
        const response = await apiClient.post<NetworkService>(
            '/api/services',
            payload,
        );

        return response.data;
    },

    async updateService(
        serviceId: number,
        payload: UpdateNetworkServiceRequest,
    ): Promise<NetworkService> {
        const response = await apiClient.put<NetworkService>(
            `/api/services/${serviceId}`,
            payload,
        );

        return response.data;
    },

    async deleteService(serviceId: number): Promise<void> {
        await apiClient.delete(`/api/services/${serviceId}`);
    },

    async enableService(serviceId: number): Promise<NetworkService> {
        const response = await apiClient.post<NetworkService>(
            `/api/services/${serviceId}/enable`,
        );

        return response.data;
    },

    async disableService(serviceId: number): Promise<NetworkService> {
        const response = await apiClient.post<NetworkService>(
            `/api/services/${serviceId}/disable`,
        );

        return response.data;
    },

    async runCheck(serviceId: number): Promise<CheckResult> {
        const response = await apiClient.post<CheckResult>(
            `/api/checks/services/${serviceId}/run`,
        );

        return response.data;
    },

    async getCheckHistory(serviceId: number): Promise<CheckResult[]> {
        const response = await apiClient.get<CheckResult[]>(
            `/api/checks/services/${serviceId}/history`,
        );

        return response.data;
    },
};