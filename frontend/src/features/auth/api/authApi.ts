import { apiClient } from '../../../shared/api/apiClient';
import type {
    CurrentUser,
    LoginRequest,
    LoginResponse,
    PasswordResetRequest,
    RegisterRequest,
} from '../model/authTypes';

export const authApi = {
    async login(payload: LoginRequest): Promise<LoginResponse> {
        const response = await apiClient.post<LoginResponse>(
            '/api/auth/login',
            payload,
        );

        return response.data;
    },

    async register(payload: RegisterRequest): Promise<LoginResponse> {
        const response = await apiClient.post<LoginResponse>(
            '/api/auth/register',
            payload,
        );

        return response.data;
    },

    async me(): Promise<CurrentUser> {
        const response = await apiClient.get<CurrentUser>('/api/auth/me');

        return response.data;
    },

    async requestPasswordReset(payload: PasswordResetRequest): Promise<void> {
        await apiClient.post('/api/auth/password-reset/request', payload);
    },
};