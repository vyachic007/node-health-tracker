import type { AxiosResponse } from 'axios';
import { apiClient } from '../../../shared/api/apiClient';
import type {
    CheckResult,
    CreateNetworkServiceRequest,
    NetworkService,
    UpdateNetworkServiceRequest,
} from '../model/serviceTypes';

export const servicesApi = {
    async getMyServices(): Promise<NetworkService[]> {
        const response: AxiosResponse<NetworkService[]> = await apiClient.get('/api/services/my');
        return response.data;
    },

    async getService(serviceId: number): Promise<NetworkService> {
        const response: AxiosResponse<NetworkService> = await apiClient.get(
            `/api/services/${serviceId}`,
        );

        return response.data;
    },

    async createService(payload: CreateNetworkServiceRequest): Promise<NetworkService> {
        const response: AxiosResponse<NetworkService> = await apiClient.post(
            '/api/services',
            payload,
        );

        return response.data;
    },

    async updateService(
        serviceId: number,
        payload: UpdateNetworkServiceRequest,
    ): Promise<NetworkService> {
        const response: AxiosResponse<NetworkService> = await apiClient.put(
            `/api/services/${serviceId}`,
            payload,
        );

        return response.data;
    },

    async deleteService(serviceId: number): Promise<void> {
        await apiClient.delete(`/api/services/${serviceId}`);
    },

    async runCheck(serviceId: number): Promise<CheckResult> {
        const response: AxiosResponse<CheckResult> = await apiClient.post(
            `/api/checks/services/${serviceId}/run`,
        );

        return response.data;
    },

    async getCheckHistory(serviceId: number): Promise<CheckResult[]> {
        const response: AxiosResponse<CheckResult[]> = await apiClient.get(
            `/api/checks/services/${serviceId}/history`,
        );

        return response.data;
    },
};