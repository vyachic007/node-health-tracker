import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SnackbarProvider } from 'notistack';
import { useState, type ReactNode } from 'react';
import { AuthProvider } from '../../features/auth/store/AuthContext';
import { AppThemeProvider } from './AppThemeProvider';

interface AppProvidersProps {
    children: ReactNode;
}

export function AppProviders({ children }: AppProvidersProps) {
    const [queryClient] = useState(
        () =>
            new QueryClient({
                defaultOptions: {
                    queries: {
                        retry: 1,
                        refetchOnWindowFocus: false,
                    },
                },
            }),
    );

    return (
        <AppThemeProvider>
            <SnackbarProvider
                maxSnack={4}
                autoHideDuration={3500}
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            >
                <QueryClientProvider client={queryClient}>
                    <AuthProvider>{children}</AuthProvider>
                </QueryClientProvider>
            </SnackbarProvider>
        </AppThemeProvider>
    );
}