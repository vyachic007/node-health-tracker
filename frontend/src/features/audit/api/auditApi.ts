import type { AxiosResponse } from 'axios';
import { apiClient } from '../../../shared/api/apiClient';
import type { AuditEvent } from '../model/auditTypes';

export const auditApi = {
    async getEvents(): Promise<AuditEvent[]> {
        const response: AxiosResponse<AuditEvent[]> = await apiClient.get(
            '/api/audit/events',
        );

        return response.data;
    },
};