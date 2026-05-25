import { apiClient } from '../../../shared/api/apiClient';
import type { AdminUser, AdminUserRole } from '../model/adminUserTypes';

interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export const adminUsersApi = {
    async getUsers(): Promise<AdminUser[]> {
        const { data } = await apiClient.get<PageResponse<AdminUser>>(
            '/api/admin/users',
            {
                params: {
                    page: 0,
                    size: 100,
                },
            },
        );

        return data.content;
    },

    async blockUser(userId: number): Promise<AdminUser> {
        const { data } = await apiClient.patch<AdminUser>(
            `/api/admin/users/${userId}/status`,
            {
                status: 'BLOCKED',
            },
        );

        return data;
    },

    async unblockUser(userId: number): Promise<AdminUser> {
        const { data } = await apiClient.patch<AdminUser>(
            `/api/admin/users/${userId}/status`,
            {
                status: 'ACTIVE',
            },
        );

        return data;
    },

    async updateUserRole(userId: number, role: AdminUserRole): Promise<AdminUser> {
        const { data } = await apiClient.patch<AdminUser>(
            `/api/admin/users/${userId}/role`,
            {
                role,
            },
        );

        return data;
    },

    async deleteUser(userId: number): Promise<AdminUser> {
        const { data } = await apiClient.delete<AdminUser>(
            `/api/admin/users/${userId}`,
        );

        return data;
    },
};