import {
    CssBaseline,
    ThemeProvider,
    createTheme,
    useMediaQuery,
} from '@mui/material';
import {
    createContext,
    useContext,
    useMemo,
    useState,
    type ReactNode,
} from 'react';

type ThemeMode = 'light' | 'dark';

interface AppThemeContextValue {
    mode: ThemeMode;
    toggleMode: () => void;
}

const THEME_KEY = 'node_health_tracker_theme';

const AppThemeContext = createContext<AppThemeContextValue | null>(null);

interface AppThemeProviderProps {
    children: ReactNode;
}

export function AppThemeProvider({ children }: AppThemeProviderProps) {
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

    const [mode, setMode] = useState<ThemeMode>(() => {
        const savedMode = localStorage.getItem(THEME_KEY);

        if (savedMode === 'light' || savedMode === 'dark') {
            return savedMode;
        }

        return prefersDarkMode ? 'dark' : 'light';
    });

    const theme = useMemo(
        () =>
            createTheme({
                palette: {
                    mode,
                    primary: {
                        main: mode === 'dark' ? '#7c9cff' : '#2458d3',
                    },
                    secondary: {
                        main: mode === 'dark' ? '#5eead4' : '#0f766e',
                    },
                    background: {
                        default: mode === 'dark' ? '#0b1020' : '#f4f7fb',
                        paper: mode === 'dark' ? '#111827' : '#ffffff',
                    },
                    success: {
                        main: '#16a34a',
                    },
                    warning: {
                        main: '#f59e0b',
                    },
                    error: {
                        main: '#dc2626',
                    },
                },
                shape: {
                    borderRadius: 16,
                },
                typography: {
                    fontFamily:
                        'Inter, Roboto, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
                    h1: {
                        fontWeight: 800,
                    },
                    h2: {
                        fontWeight: 800,
                    },
                    h3: {
                        fontWeight: 800,
                    },
                    h4: {
                        fontWeight: 800,
                    },
                    h5: {
                        fontWeight: 800,
                    },
                    h6: {
                        fontWeight: 800,
                    },
                    button: {
                        fontWeight: 700,
                        textTransform: 'none',
                    },
                },
                components: {
                    MuiCard: {
                        styleOverrides: {
                            root: {
                                backgroundImage: 'none',
                            },
                        },
                    },
                    MuiButton: {
                        styleOverrides: {
                            root: {
                                borderRadius: 12,
                            },
                        },
                    },
                    MuiPaper: {
                        styleOverrides: {
                            root: {
                                backgroundImage: 'none',
                            },
                        },
                    },
                },
            }),
        [mode],
    );

    const value = useMemo<AppThemeContextValue>(
        () => ({
            mode,
            toggleMode: () => {
                setMode((currentMode) => {
                    const nextMode = currentMode === 'light' ? 'dark' : 'light';
                    localStorage.setItem(THEME_KEY, nextMode);
                    return nextMode;
                });
            },
        }),
        [mode],
    );

    return (
        <AppThemeContext.Provider value={value}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {children}
            </ThemeProvider>
        </AppThemeContext.Provider>
    );
}

export function useAppTheme() {
    const context = useContext(AppThemeContext);

    if (!context) {
        throw new Error('useAppTheme must be used inside AppThemeProvider');
    }

    return context;
}