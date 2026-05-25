import type { AdminUserRole, AdminUserStatus } from './adminUserTypes';

export const adminUserRoleLabels: Record<AdminUserRole, string> = {
    ROLE_USER: 'Пользователь',
    ROLE_ADMIN: 'Администратор',
};

export const adminUserStatusLabels: Record<AdminUserStatus, string> = {
    ACTIVE: 'Активен',
    BLOCKED: 'Заблокирован',
};

export function getUserRoleColor(role: AdminUserRole) {
    switch (role) {
        case 'ROLE_ADMIN':
            return 'primary';
        case 'ROLE_USER':
            return 'default';
        default:
            return 'default';
    }
}

export function getUserStatusColor(status: AdminUserStatus) {
    switch (status) {
        case 'ACTIVE':
            return 'success';
        case 'BLOCKED':
            return 'error';
        default:
            return 'default';
    }
}