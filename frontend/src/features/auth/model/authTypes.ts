export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export type UserStatus = 'ACTIVE' | 'BLOCKED';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    id: number;
    email: string;
    username: string;
    status: UserStatus;
    role: UserRole;
    token: string;
    tokenType: string;
}

export interface CurrentUser {
    id: number;
    email: string;
    username: string;
    status: UserStatus;
    role: UserRole;
    createdAt: string;
    updatedAt: string;
}

export interface PasswordResetRequest {
    email: string;
}