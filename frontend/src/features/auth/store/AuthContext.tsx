import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react';
import { authApi } from '../api/authApi';
import type { CurrentUser, LoginRequest } from '../model/authTypes';

interface AuthContextValue {
    user: CurrentUser | null;
    token: string | null;
    isAuthenticated: boolean;
    isAdmin: boolean;
    isLoading: boolean;
    login: (payload: LoginRequest) => Promise<void>;
    logout: () => void;
    refreshUser: () => Promise<void>;
}

const TOKEN_KEY = 'node_health_tracker_token';

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
    const [user, setUser] = useState<CurrentUser | null>(null);
    const [isLoading, setIsLoading] = useState(Boolean(token));

    const refreshUser = useCallback(async () => {
        const currentUser = await authApi.me();
        setUser(currentUser);
    }, []);

    useEffect(() => {
        if (!token) {
            setIsLoading(false);
            return;
        }

        let isMounted = true;

        authApi
            .me()
            .then((currentUser) => {
                if (isMounted) {
                    setUser(currentUser);
                }
            })
            .catch(() => {
                localStorage.removeItem(TOKEN_KEY);
                if (isMounted) {
                    setToken(null);
                    setUser(null);
                }
            })
            .finally(() => {
                if (isMounted) {
                    setIsLoading(false);
                }
            });

        return () => {
            isMounted = false;
        };
    }, [token]);

    const login = useCallback(async (payload: LoginRequest) => {
        const response = await authApi.login(payload);

        localStorage.setItem(TOKEN_KEY, response.token);
        setToken(response.token);

        const currentUser = await authApi.me();
        setUser(currentUser);
    }, []);

    const logout = useCallback(() => {
        localStorage.removeItem(TOKEN_KEY);
        setToken(null);
        setUser(null);
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            token,
            isAuthenticated: Boolean(token && user),
            isAdmin: user?.role === 'ROLE_ADMIN',
            isLoading,
            login,
            logout,
            refreshUser,
        }),
        [user, token, isLoading, login, logout, refreshUser],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }

    return context;
}