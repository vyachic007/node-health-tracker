import type { AxiosResponse } from 'axios';
import { apiClient } from '../../../shared/api/apiClient';
import type {
    CreateNetworkNodeRequest,
    NetworkNode,
    UpdateNetworkNodeRequest,
} from '../model/nodeTypes';

export const nodesApi = {
    async getMyNodes(): Promise<NetworkNode[]> {
        const response: AxiosResponse<NetworkNode[]> = await apiClient.get('/api/nodes/my');
        return response.data;
    },

    async getNode(nodeId: number): Promise<NetworkNode> {
        const response: AxiosResponse<NetworkNode> = await apiClient.get(`/api/nodes/${nodeId}`);
        return response.data;
    },

    async createNode(payload: CreateNetworkNodeRequest): Promise<NetworkNode> {
        const response: AxiosResponse<NetworkNode> = await apiClient.post('/api/nodes', payload);
        return response.data;
    },

    async updateNode(
        nodeId: number,
        payload: UpdateNetworkNodeRequest,
    ): Promise<NetworkNode> {
        const response: AxiosResponse<NetworkNode> = await apiClient.put(
            `/api/nodes/${nodeId}`,
            payload,
        );

        return response.data;
    },

    async deleteNode(nodeId: number): Promise<void> {
        await apiClient.delete(`/api/nodes/${nodeId}`);
    },
};