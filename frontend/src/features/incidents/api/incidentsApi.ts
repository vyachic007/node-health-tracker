import type { AxiosResponse } from 'axios';
import { apiClient } from '../../../shared/api/apiClient';
import type {
    Incident,
    IncidentRecurrenceAnalysis,
    IncidentRecoveryChecklist,
    IncidentReport,
    IncidentTimelineEvent,
} from '../model/incidentTypes';

export const incidentsApi = {
    async getMyIncidents(): Promise<Incident[]> {
        const response: AxiosResponse<Incident[]> = await apiClient.get('/api/incidents/my');
        return response.data;
    },

    async getIncident(incidentId: number): Promise<Incident> {
        const response: AxiosResponse<Incident> = await apiClient.get(
            `/api/incidents/${incidentId}`,
        );

        return response.data;
    },

    async getIncidentTimeline(incidentId: number): Promise<IncidentTimelineEvent[]> {
        const response: AxiosResponse<IncidentTimelineEvent[]> = await apiClient.get(
            `/api/incidents/${incidentId}/timeline`,
        );

        return response.data;
    },

    async getRecoveryChecklist(incidentId: number): Promise<IncidentRecoveryChecklist> {
        const response: AxiosResponse<IncidentRecoveryChecklist> = await apiClient.get(
            `/api/incidents/${incidentId}/recovery-checklist`,
        );

        return response.data;
    },

    async getIncidentReport(incidentId: number): Promise<IncidentReport> {
        const response: AxiosResponse<IncidentReport> = await apiClient.get(
            `/api/incidents/${incidentId}/report`,
        );

        return response.data;
    },

    async getRecurrenceAnalysis(incidentId: number): Promise<IncidentRecurrenceAnalysis> {
        const response: AxiosResponse<IncidentRecurrenceAnalysis> = await apiClient.get(
            `/api/incidents/${incidentId}/recurrence-analysis`,
        );

        return response.data;
    },
};