import {
    Alert,
    Card,
    CardContent,
    Grid,
    LinearProgress,
    Stack,
    Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../shared/api/apiClient';

interface AdminDashboardResponse {
    totalUsers: number;
    activeUsers: number;
    blockedUsers: number;
    totalNodes: number;
    totalServices: number;
    upServices: number;
    downServices: number;
    openIncidents: number;
    resolvedIncidents: number;
    checksLast24Hours: number;
    averageHealthScore: number;
    averageHealthLevel: string;
    availabilityPercent24h: number;
    averageResponseTimeMs24h: number | null;
}

export function AdminDashboardPage() {
    const { data, isLoading, isError } = useQuery({
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
        return <Alert severity="error">Не удалось загрузить admin dashboard.</Alert>;
    }

    const cards = [
        { title: 'Пользователи', value: data.totalUsers, subtitle: `Активные: ${data.activeUsers}` },
        { title: 'Узлы', value: data.totalNodes, subtitle: 'Всего в системе' },
        { title: 'Сервисы', value: data.totalServices, subtitle: `UP: ${data.upServices} / DOWN: ${data.downServices}` },
        { title: 'Инциденты', value: data.openIncidents, subtitle: `Закрыто: ${data.resolvedIncidents}` },
        { title: 'Проверки за 24 часа', value: data.checksLast24Hours, subtitle: 'Системная активность' },
        { title: 'Health score', value: data.averageHealthScore, subtitle: data.averageHealthLevel },
    ];

    return (
        <Stack spacing={3}>
            <div>
                <Typography variant="h4">Admin dashboard</Typography>
                <Typography color="text.secondary">
                    Сводная информация по всей платформе мониторинга.
                </Typography>
            </div>

            <Grid container spacing={2}>
                {cards.map((card) => (
                    <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 4 }}>
                        <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                            <CardContent>
                                <Stack spacing={1}>
                                    <Typography color="text.secondary" sx={{ fontWeight: 700 }}>
                                        {card.title}
                                    </Typography>
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
        </Stack>
    );
}