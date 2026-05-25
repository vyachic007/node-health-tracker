import {
    Alert,
    Box,
    Card,
    CardContent,
    Chip,
    Grid,
    LinearProgress,
    Stack,
    Typography,
} from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import HubIcon from '@mui/icons-material/Hub';
import DnsIcon from '@mui/icons-material/Dns';
import WarningIcon from '@mui/icons-material/Warning';
import SpeedIcon from '@mui/icons-material/Speed';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../shared/api/apiClient';

type HealthLevel = 'HEALTHY' | 'DEGRADED' | 'UNSTABLE' | 'CRITICAL';

interface AdminDashboardResponse {
    totalUsers: number;
    activeUsers: number;
    blockedUsers: number;
    totalNodes: number;
    activeNodes: number;
    totalServices: number;
    upServices: number;
    downServices: number;
    openIncidents: number;
    resolvedIncidents: number;
    averageHealthScore: number;
    averageHealthLevel: HealthLevel;
    availabilityPercent24h: number;
    averageResponseTimeMs24h: number;
    totalChecks24h: number;
}

function getHealthLevelLabel(level: HealthLevel) {
    switch (level) {
        case 'HEALTHY':
            return 'Стабильное состояние';
        case 'DEGRADED':
            return 'Есть ухудшения';
        case 'UNSTABLE':
            return 'Нестабильное состояние';
        case 'CRITICAL':
            return 'Критическое состояние';
        default:
            return level;
    }
}

function getHealthLevelChipColor(level: HealthLevel) {
    switch (level) {
        case 'HEALTHY':
            return 'success';
        case 'DEGRADED':
        case 'UNSTABLE':
            return 'warning';
        case 'CRITICAL':
            return 'error';
        default:
            return 'default';
    }
}

function formatPercent(value: number) {
    return `${Number(value ?? 0).toFixed(1)}%`;
}

function formatResponseTime(value: number) {
    return `${Number(value ?? 0).toFixed(2)} мс`;
}

export function AdminDashboardPage() {
    const {
        data,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['dashboard', 'admin'],
        queryFn: async () => {
            const response = await apiClient.get<AdminDashboardResponse>('/api/dashboard/admin');
            return response.data;
        },
    });

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError || !data) {
        return (
            <Alert severity="error">
                Не удалось загрузить административную панель мониторинга.
            </Alert>
        );
    }

    const cards = [
        {
            title: 'Пользователи',
            value: data.totalUsers,
            subtitle: `Активные: ${data.activeUsers} / Заблокированные: ${data.blockedUsers}`,
            icon: <PeopleIcon color="primary" />,
        },
        {
            title: 'Узлы',
            value: data.totalNodes,
            subtitle: `Активные: ${data.activeNodes}`,
            icon: <HubIcon color="primary" />,
        },
        {
            title: 'Сервисы',
            value: data.totalServices,
            subtitle: `Доступно: ${data.upServices} / Недоступно: ${data.downServices}`,
            icon: <DnsIcon color="primary" />,
        },
        {
            title: 'Открытые инциденты',
            value: data.openIncidents,
            subtitle: `Закрыто: ${data.resolvedIncidents}`,
            icon: <WarningIcon color="primary" />,
        },
        {
            title: 'Оценка состояния',
            value: data.averageHealthScore,
            subtitle: getHealthLevelLabel(data.averageHealthLevel),
            icon: <SpeedIcon color="primary" />,
        },
    ];

    return (
        <Stack spacing={3}>
            <Box>
                <Typography variant="h4">
                    Административная панель мониторинга
                </Typography>

                <Typography color="text.secondary">
                    Общая сводка по пользователям, узлам, сервисам и инцидентам системы.
                </Typography>
            </Box>

            <Grid container spacing={2}>
                {cards.map((card) => (
                    <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                        <Card
                            elevation={0}
                            sx={{
                                height: '100%',
                                border: 1,
                                borderColor: 'divider',
                            }}
                        >
                            <CardContent>
                                <Stack spacing={2}>
                                    <Stack
                                        direction="row"
                                        sx={{
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                        }}
                                    >
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {card.title}
                                        </Typography>

                                        {card.icon}
                                    </Stack>

                                    <Typography variant="h3">
                                        {card.value}
                                    </Typography>

                                    <Typography color="text.secondary">
                                        {card.subtitle}
                                    </Typography>
                                </Stack>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Card
                elevation={0}
                sx={{
                    border: 1,
                    borderColor: 'divider',
                }}
            >
                <CardContent>
                    <Stack spacing={2}>
                        <Stack
                            direction={{ xs: 'column', sm: 'row' }}
                            spacing={2}
                            sx={{
                                justifyContent: 'space-between',
                                alignItems: { xs: 'flex-start', sm: 'center' },
                            }}
                        >
                            <Box>
                                <Typography variant="h6">
                                    Состояние мониторинга
                                </Typography>

                                <Typography color="text.secondary">
                                    Сводные показатели доступности, задержки и количества проверок.
                                </Typography>
                            </Box>

                            <Chip
                                label={getHealthLevelLabel(data.averageHealthLevel)}
                                color={getHealthLevelChipColor(data.averageHealthLevel)}
                                variant="outlined"
                            />
                        </Stack>

                        <Grid container spacing={3}>
                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">
                                    Доступность за 24 часа
                                </Typography>

                                <Typography variant="h4">
                                    {formatPercent(data.availabilityPercent24h)}
                                </Typography>
                            </Grid>

                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">
                                    Среднее время ответа
                                </Typography>

                                <Typography variant="h4">
                                    {formatResponseTime(data.averageResponseTimeMs24h)}
                                </Typography>
                            </Grid>

                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">
                                    Проверок за 24 часа
                                </Typography>

                                <Typography variant="h4">
                                    {data.totalChecks24h}
                                </Typography>
                            </Grid>
                        </Grid>
                    </Stack>
                </CardContent>
            </Card>
        </Stack>
    );
}