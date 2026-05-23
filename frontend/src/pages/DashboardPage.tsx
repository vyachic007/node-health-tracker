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
import { useQuery } from '@tanstack/react-query';
import DnsIcon from '@mui/icons-material/Dns';
import HubIcon from '@mui/icons-material/Hub';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import SpeedIcon from '@mui/icons-material/Speed';
import { apiClient } from '../shared/api/apiClient';

interface DashboardResponse {
    totalNodes: number;
    activeNodes: number;
    inactiveNodes: number;
    totalServices: number;
    enabledServices: number;
    disabledServices: number;
    upServices: number;
    downServices: number;
    unknownServices: number;
    openIncidents: number;
    resolvedIncidents: number;
    checksLast24Hours: number;
    averageHealthScore: number;
    averageHealthLevel: string;
    healthyServices: number;
    degradedServices: number;
    unstableServices: number;
    criticalServices: number;
    availabilityPercent24h: number;
    averageResponseTimeMs24h: number | null;
}

function getHealthColor(level: string) {
    if (level === 'HEALTHY') {
        return 'success';
    }

    if (level === 'DEGRADED') {
        return 'warning';
    }

    if (level === 'UNSTABLE') {
        return 'warning';
    }

    return 'error';
}

export function DashboardPage() {
    const { data, isLoading, isError } = useQuery({
        queryKey: ['dashboard', 'my'],
        queryFn: async () => {
            const response = await apiClient.get<DashboardResponse>('/api/dashboard/my');
            return response.data;
        },
    });

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError || !data) {
        return <Alert severity="error">Не удалось загрузить dashboard.</Alert>;
    }

    const cards = [
        {
            title: 'Узлы',
            value: data.totalNodes,
            subtitle: `Активные: ${data.activeNodes}`,
            icon: <HubIcon />,
        },
        {
            title: 'Сервисы',
            value: data.totalServices,
            subtitle: `UP: ${data.upServices} / DOWN: ${data.downServices}`,
            icon: <DnsIcon />,
        },
        {
            title: 'Открытые инциденты',
            value: data.openIncidents,
            subtitle: `Закрыто: ${data.resolvedIncidents}`,
            icon: <ReportProblemIcon />,
        },
        {
            title: 'Health score',
            value: data.averageHealthScore,
            subtitle: data.averageHealthLevel,
            icon: <SpeedIcon />,
        },
    ];

    return (
        <Stack spacing={3}>
            <Box>
                <Typography variant="h4">Dashboard</Typography>
                <Typography color="text.secondary">
                    Общая картина состояния ваших узлов, сервисов и инцидентов.
                </Typography>
            </Box>

            <Grid container spacing={2}>
                {cards.map((card) => (
                    <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                        <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                            <CardContent>
                                <Stack spacing={2}>
                                    <Stack direction="row" justifyContent="space-between" alignItems="center">
                                        <Typography color="text.secondary" fontWeight={700}>
                                            {card.title}
                                        </Typography>

                                        <Box sx={{ color: 'primary.main' }}>{card.icon}</Box>
                                    </Stack>

                                    <Typography variant="h4">{card.value}</Typography>

                                    <Typography variant="body2" color="text.secondary">
                                        {card.subtitle}
                                    </Typography>
                                </Stack>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Stack spacing={2}>
                        <Stack direction="row" justifyContent="space-between" alignItems="center">
                            <Box>
                                <Typography variant="h6">Состояние мониторинга</Typography>
                                <Typography color="text.secondary">
                                    Доступность за 24 часа и средняя задержка ответа.
                                </Typography>
                            </Box>

                            <Chip
                                label={data.averageHealthLevel}
                                color={getHealthColor(data.averageHealthLevel)}
                                variant="outlined"
                            />
                        </Stack>

                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">Доступность за 24 часа</Typography>
                                <Typography variant="h5">{data.availabilityPercent24h}%</Typography>
                            </Grid>

                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">Среднее время ответа</Typography>
                                <Typography variant="h5">
                                    {data.averageResponseTimeMs24h ?? '—'} мс
                                </Typography>
                            </Grid>

                            <Grid size={{ xs: 12, md: 4 }}>
                                <Typography color="text.secondary">Проверок за 24 часа</Typography>
                                <Typography variant="h5">{data.checksLast24Hours}</Typography>
                            </Grid>
                        </Grid>
                    </Stack>
                </CardContent>
            </Card>
        </Stack>
    );
}