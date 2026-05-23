import {
    AppBar,
    Avatar,
    Box,
    Divider,
    Drawer,
    IconButton,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Stack,
    Toolbar,
    Tooltip,
    Typography,
    useMediaQuery,
    useTheme,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import DnsIcon from '@mui/icons-material/Dns';
import HubIcon from '@mui/icons-material/Hub';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import NotificationsIcon from '@mui/icons-material/Notifications';
import HistoryIcon from '@mui/icons-material/History';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import LogoutIcon from '@mui/icons-material/Logout';
import MenuIcon from '@mui/icons-material/Menu';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAppTheme } from '../app/providers/AppThemeProvider';
import { useAuth } from '../features/auth/store/AuthContext';

const drawerWidth = 280;

const userNavigation = [
    { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
    { label: 'Узлы', path: '/nodes', icon: <HubIcon /> },
    { label: 'Сервисы', path: '/services', icon: <DnsIcon /> },
    { label: 'Инциденты', path: '/incidents', icon: <ReportProblemIcon /> },
    { label: 'Уведомления', path: '/notifications', icon: <NotificationsIcon /> },
    { label: 'Аудит', path: '/audit', icon: <HistoryIcon /> },
];

const adminNavigation = [
    { label: 'Admin dashboard', path: '/admin/dashboard', icon: <AdminPanelSettingsIcon /> },
    { label: 'Пользователи', path: '/admin/users', icon: <AdminPanelSettingsIcon /> },
    { label: 'Admin аудит', path: '/admin/audit', icon: <HistoryIcon /> },
];

export function AppLayout() {
    const theme = useTheme();
    const isDesktop = useMediaQuery(theme.breakpoints.up('lg'));
    const location = useLocation();
    const navigate = useNavigate();

    const { mode, toggleMode } = useAppTheme();
    const { user, isAdmin, logout } = useAuth();

    const [isMobileOpen, setIsMobileOpen] = useState(false);

    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
    };

    const drawer = (
        <Stack sx={{ height: '100%' }}>
            <Stack spacing={1.5} sx={{ p: 2.5 }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                    <Avatar
                        variant="rounded"
                        sx={{
                            bgcolor: 'primary.main',
                            color: 'primary.contrastText',
                            width: 44,
                            height: 44,
                        }}
                    >
                        <MonitorHeartIcon />
                    </Avatar>

                    <Box>
                        <Typography fontWeight={900}>Node Health</Typography>
                        <Typography variant="caption" color="text.secondary">
                            Diagnostic monitoring
                        </Typography>
                    </Box>
                </Stack>
            </Stack>

            <Divider />

            <Box sx={{ flex: 1, px: 1.5, py: 2 }}>
                <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ px: 1.5, mb: 1, display: 'block', fontWeight: 700 }}
                >
                    Рабочая область
                </Typography>

                <List disablePadding>
                    {userNavigation.map((item) => {
                        const selected = location.pathname === item.path;

                        return (
                            <ListItemButton
                                key={item.path}
                                component={NavLink}
                                to={item.path}
                                selected={selected}
                                onClick={() => setIsMobileOpen(false)}
                                sx={{ borderRadius: 3, mb: 0.5 }}
                            >
                                <ListItemIcon>{item.icon}</ListItemIcon>
                                <ListItemText primary={item.label} />
                            </ListItemButton>
                        );
                    })}
                </List>

                {isAdmin && (
                    <>
                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{ px: 1.5, mt: 3, mb: 1, display: 'block', fontWeight: 700 }}
                        >
                            Администрирование
                        </Typography>

                        <List disablePadding>
                            {adminNavigation.map((item) => {
                                const selected = location.pathname === item.path;

                                return (
                                    <ListItemButton
                                        key={item.path}
                                        component={NavLink}
                                        to={item.path}
                                        selected={selected}
                                        onClick={() => setIsMobileOpen(false)}
                                        sx={{ borderRadius: 3, mb: 0.5 }}
                                    >
                                        <ListItemIcon>{item.icon}</ListItemIcon>
                                        <ListItemText primary={item.label} />
                                    </ListItemButton>
                                );
                            })}
                        </List>
                    </>
                )}
            </Box>

            <Divider />

            <Box sx={{ p: 2 }}>
                <Stack spacing={1}>
                    <Typography variant="body2" fontWeight={800}>
                        {user?.username}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                        {user?.email}
                    </Typography>

                    <ListItemButton onClick={handleLogout} sx={{ borderRadius: 3 }}>
                        <ListItemIcon>
                            <LogoutIcon />
                        </ListItemIcon>
                        <ListItemText primary="Выйти" />
                    </ListItemButton>
                </Stack>
            </Box>
        </Stack>
    );

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
            <AppBar
                position="fixed"
                elevation={0}
                color="transparent"
                sx={{
                    backdropFilter: 'blur(18px)',
                    borderBottom: 1,
                    borderColor: 'divider',
                    width: { lg: `calc(100% - ${drawerWidth}px)` },
                    ml: { lg: `${drawerWidth}px` },
                }}
            >
                <Toolbar>
                    {!isDesktop && (
                        <IconButton edge="start" onClick={() => setIsMobileOpen(true)} sx={{ mr: 1 }}>
                            <MenuIcon />
                        </IconButton>
                    )}

                    <Box sx={{ flex: 1 }}>
                        <Typography variant="h6" fontWeight={900}>
                            Node Health Tracker
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Диагностика, инциденты, уведомления и health score
                        </Typography>
                    </Box>

                    <Tooltip title={mode === 'dark' ? 'Светлая тема' : 'Тёмная тема'}>
                        <IconButton onClick={toggleMode}>
                            {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
                        </IconButton>
                    </Tooltip>
                </Toolbar>
            </AppBar>

            <Box component="nav" sx={{ width: { lg: drawerWidth }, flexShrink: { lg: 0 } }}>
                <Drawer
                    variant="temporary"
                    open={isMobileOpen}
                    onClose={() => setIsMobileOpen(false)}
                    ModalProps={{ keepMounted: true }}
                    sx={{
                        display: { xs: 'block', lg: 'none' },
                        '& .MuiDrawer-paper': { width: drawerWidth },
                    }}
                >
                    {drawer}
                </Drawer>

                <Drawer
                    variant="permanent"
                    sx={{
                        display: { xs: 'none', lg: 'block' },
                        '& .MuiDrawer-paper': {
                            width: drawerWidth,
                            borderRight: 1,
                            borderColor: 'divider',
                        },
                    }}
                    open
                >
                    {drawer}
                </Drawer>
            </Box>

            <Box
                component="main"
                sx={{
                    flex: 1,
                    minWidth: 0,
                    px: { xs: 2, md: 3 },
                    py: 3,
                    mt: 9,
                }}
            >
                <Outlet />
            </Box>
        </Box>
    );
}