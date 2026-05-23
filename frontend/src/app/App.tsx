import { CircularProgress, Stack } from '@mui/material';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import type { JSX } from 'react';
import { AppProviders } from './providers/AppProviders';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { PasswordResetPage } from '../features/auth/pages/PasswordResetPage';
import { useAuth } from '../features/auth/store/AuthContext';
import { IncidentsPage } from '../features/incidents/pages/IncidentsPage';
import { NodesPage } from '../features/nodes/pages/NodesPage';
import { ServicesPage } from '../features/services/pages/ServicesPage';
import { ServiceDetailsPage } from '../features/services/pages/ServiceDetailsPage';
import { AppLayout } from '../layouts/AppLayout';
import { AdminDashboardPage } from '../pages/AdminDashboardPage';
import { DashboardPage } from '../pages/DashboardPage';
import { ForbiddenPage } from '../pages/ForbiddenPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { PlaceholderPage } from '../pages/PlaceholderPage';

interface ProtectedRouteProps {
    children: JSX.Element;
    adminOnly?: boolean;
}

function ProtectedRoute({ children, adminOnly = false }: ProtectedRouteProps) {
    const { isAuthenticated, isAdmin, isLoading } = useAuth();

    if (isLoading) {
        return (
            <Stack
                sx={{
                    minHeight: '100vh',
                    alignItems: 'center',
                    justifyContent: 'center',
                }}
            >
                <CircularProgress />
            </Stack>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (adminOnly && !isAdmin) {
        return <Navigate to="/forbidden" replace />;
    }

    return children;
}

function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/password-reset" element={<PasswordResetPage />} />

                <Route
                    element={
                        <ProtectedRoute>
                            <AppLayout />
                        </ProtectedRoute>
                    }
                >
                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<DashboardPage />} />

                    <Route path="/nodes" element={<NodesPage />} />

                    <Route path="/services" element={<ServicesPage />} />
                    <Route path="/services/:serviceId" element={<ServiceDetailsPage />} />

                    <Route path="/incidents" element={<IncidentsPage />} />

                    <Route
                        path="/notifications"
                        element={
                            <PlaceholderPage
                                title="Уведомления"
                                description="Настройки email, Telegram и VK-уведомлений."
                            />
                        }
                    />

                    <Route
                        path="/audit"
                        element={
                            <PlaceholderPage
                                title="Аудит"
                                description="Журнал действий пользователя и системных событий."
                            />
                        }
                    />

                    <Route
                        path="/admin/dashboard"
                        element={
                            <ProtectedRoute adminOnly>
                                <AdminDashboardPage />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/admin/users"
                        element={
                            <ProtectedRoute adminOnly>
                                <PlaceholderPage
                                    title="Пользователи"
                                    description="Администрирование пользователей, статусов и ролей."
                                />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/admin/audit"
                        element={
                            <ProtectedRoute adminOnly>
                                <PlaceholderPage
                                    title="Админский аудит"
                                    description="Глобальный журнал действий по всей платформе."
                                />
                            </ProtectedRoute>
                        }
                    />

                    <Route path="/forbidden" element={<ForbiddenPage />} />
                </Route>

                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export function App() {
    return (
        <AppProviders>
            <AppRoutes />
        </AppProviders>
    );
}