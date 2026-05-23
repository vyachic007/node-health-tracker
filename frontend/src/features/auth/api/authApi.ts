import type { AxiosResponse } from 'axios';
import { apiClient } from '../../../shared/api/apiClient';
import type {
    CurrentUser,
    LoginRequest,
    LoginResponse,
    PasswordResetRequest,
} from '../model/authTypes';

export const authApi = {
    async login(payload: LoginRequest): Promise<LoginResponse> {
        const response: AxiosResponse<LoginResponse> = await apiClient.post(
            '/api/auth/login',
            payload,
        );

        return response.data;
    },

    async me(): Promise<CurrentUser> {
        const response: AxiosResponse<CurrentUser> = await apiClient.get('/api/auth/me');

        return response.data;
    },

    async requestPasswordReset(payload: PasswordResetRequest): Promise<void> {
        await apiClient.post('/api/auth/password-reset/request', payload);
    },
};