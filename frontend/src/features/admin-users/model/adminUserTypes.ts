export type AdminUserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export type AdminUserStatus = 'ACTIVE' | 'BLOCKED';

export interface AdminUser {
    id: number;
    username: string;
    email: string;
    role: AdminUserRole;
    status: AdminUserStatus;
    createdAt: string;
    updatedAt: string;
}