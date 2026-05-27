import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Grid,
    LinearProgress,
    Stack,
    Typography,
    alpha,
    useTheme,
} from '@mui/material';
import HubIcon from '@mui/icons-material/Hub';
import DnsIcon from '@mui/icons-material/Dns';
import WarningIcon from '@mui/icons-material/Warning';
import SpeedIcon from '@mui/icons-material/Speed';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import TimelineIcon from '@mui/icons-material/Timeline';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { apiClient } from '../shared/api/apiClient';

type HealthLevel = 'HEALTHY' | 'DEGRADED' | 'UNSTABLE' | 'CRITICAL';

interface DashboardResponse {
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
    checksLast24Hours: number;
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

function getAvailabilityTone(value: number): 'success' | 'warning' | 'error' {
    if (value >= 95) {
        return 'success';
    }

    if (value >= 70) {
        return 'warning';
    }

    return 'error';
}

function getAvailabilityProgress(value: number) {
    return Math.max(0, Math.min(Number(value ?? 0), 100));
}

function formatPercent(value: number) {
    return `${Number(value ?? 0).toFixed(1)}%`;
}

function formatResponseTime(value: number) {
    return `${Number(value ?? 0).toFixed(2)} мс`;
}

export function DashboardPage() {
    const theme = useTheme();

    const {
        data,
        isLoading,
        isError,
    } = useQuery({
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
        return (
            <Alert severity="error">
                Не удалось загрузить панель мониторинга.
            </Alert>
        );
    }

    const availabilityProgress = getAvailabilityProgress(data.availabilityPercent24h);
    const availabilityTone = getAvailabilityTone(data.availabilityPercent24h);
    const availabilityColor = theme.palette[availabilityTone].main;

    const unavailablePercent =
        data.totalServices > 0
            ? Math.round((data.downServices / data.totalServices) * 100)
            : 0;

    const checksLast24Hours = data.checksLast24Hours ?? 0;

    const cards = [
        {
            title: 'Узлы',
            value: data.totalNodes,
            subtitle: `Активные: ${data.activeNodes}`,
            icon: <HubIcon fontSize="small" />,
            link: '/nodes',
            linkText: 'Открыть',
        },
        {
            title: 'Сервисы',
            value: data.totalServices,
            subtitle: `Работают: ${data.upServices} / Недоступны: ${data.downServices}`,
            icon: <DnsIcon fontSize="small" />,
            link: '/services',
            linkText: 'Открыть',
        },
        {
            title: 'Инциденты',
            value: data.openIncidents,
            subtitle: `Закрыто: ${data.resolvedIncidents}`,
            icon: <WarningIcon fontSize="small" />,
            link: '/incidents',
            linkText: 'Открыть',
        },
        {
            title: 'Health Score',
            value: data.averageHealthScore,
            subtitle: getHealthLevelLabel(data.averageHealthLevel),
            icon: <SpeedIcon fontSize="small" />,
            link: '/services',
            linkText: 'Диагностика',
        },
    ];

    return (
        <Stack spacing={2.5}>
            <Card
                elevation={0}
                sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1.5,
                    background:
                        theme.palette.mode === 'dark'
                            ? `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.18)}, ${theme.palette.background.paper} 48%)`
                            : `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.10)}, ${theme.palette.background.paper} 52%)`,
                }}
            >
                <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
                    <Grid container spacing={3} sx={{ alignItems: 'center' }}>
                        <Grid size={{ xs: 12, md: 8 }}>
                            <Stack spacing={1.75}>
                                <Stack
                                    direction="row"
                                    spacing={1}
                                    useFlexGap
                                    sx={{ flexWrap: 'wrap', alignItems: 'center' }}
                                >
                                    <Chip
                                        label="Диагностический мониторинг"
                                        color="primary"
                                        variant="outlined"
                                        size="small"
                                    />

                                    <Chip
                                        label={getHealthLevelLabel(data.averageHealthLevel)}
                                        color={getHealthLevelChipColor(data.averageHealthLevel)}
                                        size="small"
                                    />
                                </Stack>

                                <Box>
                                    <Typography
                                        variant="h3"
                                        sx={{
                                            fontWeight: 900,
                                            fontSize: { xs: 34, md: 44 },
                                            lineHeight: 1.05,
                                        }}
                                    >
                                        Панель мониторинга
                                    </Typography>

                                    <Typography
                                        color="text.secondary"
                                        sx={{
                                            mt: 1,
                                            maxWidth: 760,
                                            fontSize: 15,
                                        }}
                                    >
                                        Общая картина состояния узлов, сервисов, инцидентов,
                                        доступности и качества ответа за последние 24 часа.
                                    </Typography>
                                </Box>

                                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
                                    <Button
                                        component={Link}
                                        to="/services"
                                        variant="contained"
                                        size="medium"
                                        endIcon={<ArrowForwardIcon />}
                                    >
                                        Перейти к сервисам
                                    </Button>

                                    <Button
                                        component={Link}
                                        to="/incidents"
                                        variant="outlined"
                                        size="medium"
                                        endIcon={<ArrowForwardIcon />}
                                    >
                                        Смотреть инциденты
                                    </Button>
                                </Stack>
                            </Stack>
                        </Grid>

                        <Grid size={{ xs: 12, md: 4 }}>
                            <Box
                                sx={{
                                    border: 1,
                                    borderColor: 'divider',
                                    borderRadius: 1.5,
                                    p: 2.25,
                                    bgcolor: 'background.paper',
                                }}
                            >
                                <Typography color="text.secondary" sx={{ fontSize: 14 }}>
                                    Общая оценка состояния
                                </Typography>

                                <Stack direction="row" spacing={1} sx={{ alignItems: 'baseline', mt: 0.75 }}>
                                    <Typography
                                        sx={{
                                            fontWeight: 900,
                                            fontSize: 54,
                                            lineHeight: 1,
                                        }}
                                    >
                                        {data.averageHealthScore}
                                    </Typography>

                                    <Typography variant="h6" color="text.secondary">
                                        /100
                                    </Typography>
                                </Stack>

                                <LinearProgress
                                    variant="determinate"
                                    value={data.averageHealthScore}
                                    sx={{
                                        mt: 1.75,
                                        height: 7,
                                        borderRadius: 999,
                                        bgcolor: 'action.hover',
                                    }}
                                />

                                <Typography color="text.secondary" sx={{ mt: 1.25, fontSize: 14 }}>
                                    {getHealthLevelLabel(data.averageHealthLevel)}
                                </Typography>
                            </Box>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            <Grid container spacing={2}>
                {cards.map((card) => (
                    <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                        <Card
                            elevation={0}
                            sx={{
                                height: '100%',
                                border: 1,
                                borderColor: 'divider',
                                borderRadius: 1.5,
                            }}
                        >
                            <CardContent sx={{ p: 2 }}>
                                <Stack spacing={1.4}>
                                    <Stack
                                        direction="row"
                                        sx={{
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                        }}
                                    >
                                        <Typography sx={{ fontWeight: 800, fontSize: 15 }}>
                                            {card.title}
                                        </Typography>

                                        <Box
                                            sx={{
                                                width: 32,
                                                height: 32,
                                                borderRadius: 1.5,
                                                display: 'grid',
                                                placeItems: 'center',
                                                bgcolor: alpha(theme.palette.primary.main, 0.12),
                                                color: 'primary.main',
                                            }}
                                        >
                                            {card.icon}
                                        </Box>
                                    </Stack>

                                    <Typography sx={{ fontSize: 36, fontWeight: 900, lineHeight: 1 }}>
                                        {card.value}
                                    </Typography>

                                    <Typography color="text.secondary" sx={{ fontSize: 14 }}>
                                        {card.subtitle}
                                    </Typography>

                                    <Button
                                        component={Link}
                                        to={card.link}
                                        size="small"
                                        endIcon={<ArrowForwardIcon />}
                                        sx={{ alignSelf: 'flex-start', px: 0, minWidth: 0 }}
                                    >
                                        {card.linkText}
                                    </Button>
                                </Stack>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, lg: 8 }}>
                    <Card
                        elevation={0}
                        sx={{
                            border: 1,
                            borderColor: 'divider',
                            borderRadius: 1.5,
                            height: '100%',
                        }}
                    >
                        <CardContent sx={{ p: 2.5 }}>
                            <Stack spacing={2.25}>
                                <Stack
                                    direction={{ xs: 'column', sm: 'row' }}
                                    spacing={1.5}
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: { xs: 'flex-start', sm: 'center' },
                                    }}
                                >
                                    <Box>
                                        <Typography variant="h6" sx={{ fontWeight: 900 }}>
                                            Состояние мониторинга
                                        </Typography>

                                        <Typography color="text.secondary" sx={{ fontSize: 14 }}>
                                            Доступность, задержка ответа и количество проверок за 24 часа.
                                        </Typography>
                                    </Box>

                                    <Chip
                                        label={getHealthLevelLabel(data.averageHealthLevel)}
                                        color={getHealthLevelChipColor(data.averageHealthLevel)}
                                        variant="outlined"
                                        size="small"
                                    />
                                </Stack>

                                <Box>
                                    <Stack
                                        direction="row"
                                        sx={{
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                            mb: 0.75,
                                        }}
                                    >
                                        <Typography sx={{ fontWeight: 800, fontSize: 15 }}>
                                            Доступность за 24 часа
                                        </Typography>

                                        <Typography sx={{ fontWeight: 900, color: availabilityColor }}>
                                            {formatPercent(data.availabilityPercent24h)}
                                        </Typography>
                                    </Stack>

                                    <LinearProgress
                                        variant="determinate"
                                        value={availabilityProgress}
                                        sx={{
                                            height: 8,
                                            borderRadius: 999,
                                            bgcolor: 'action.hover',
                                            '& .MuiLinearProgress-bar': {
                                                borderRadius: 999,
                                                bgcolor: availabilityColor,
                                            },
                                        }}
                                    />
                                </Box>

                                <Grid container spacing={1.5}>
                                    <Grid size={{ xs: 12, sm: 4 }}>
                                        <Box
                                            sx={{
                                                p: 1.5,
                                                borderRadius: 1.5,
                                                bgcolor: alpha(theme.palette.success.main, 0.08),
                                            }}
                                        >
                                            <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                                                <CheckCircleIcon color="success" fontSize="small" />

                                                <Box>
                                                    <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                                        Доступные
                                                    </Typography>

                                                    <Typography variant="h5" sx={{ fontWeight: 900 }}>
                                                        {data.upServices}
                                                    </Typography>
                                                </Box>
                                            </Stack>
                                        </Box>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 4 }}>
                                        <Box
                                            sx={{
                                                p: 1.5,
                                                borderRadius: 1.5,
                                                bgcolor: alpha(theme.palette.error.main, 0.08),
                                            }}
                                        >
                                            <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                                                <ErrorIcon color="error" fontSize="small" />

                                                <Box>
                                                    <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                                        Недоступные
                                                    </Typography>

                                                    <Typography variant="h5" sx={{ fontWeight: 900 }}>
                                                        {data.downServices}
                                                    </Typography>
                                                </Box>
                                            </Stack>
                                        </Box>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 4 }}>
                                        <Box
                                            sx={{
                                                p: 1.5,
                                                borderRadius: 1.5,
                                                bgcolor: alpha(theme.palette.primary.main, 0.08),
                                            }}
                                        >
                                            <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                                                <TimelineIcon color="primary" fontSize="small" />

                                                <Typography sx={{ fontSize: 14, fontWeight: 800 }}>
                                                    Проверок за 24 часа: {checksLast24Hours}
                                                </Typography>
                                            </Stack>
                                        </Box>
                                    </Grid>
                                </Grid>

                                <Grid container spacing={1.5}>
                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Box>
                                            <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                                Среднее время ответа
                                            </Typography>

                                            <Typography variant="h5" sx={{ fontWeight: 900 }}>
                                                {formatResponseTime(data.averageResponseTimeMs24h)}
                                            </Typography>
                                        </Box>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Box>
                                            <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                                Диагностический статус
                                            </Typography>

                                            <Typography variant="h5" sx={{ fontWeight: 900 }}>
                                                {getHealthLevelLabel(data.averageHealthLevel)}
                                            </Typography>
                                        </Box>
                                    </Grid>
                                </Grid>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, lg: 4 }}>
                    <Card
                        elevation={0}
                        sx={{
                            border: 1,
                            borderColor: data.openIncidents > 0 ? 'error.main' : 'divider',
                            borderRadius: 1.5,
                            height: '100%',
                        }}
                    >
                        <CardContent sx={{ p: 2.5 }}>
                            <Stack spacing={2}>
                                <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                                    <Box
                                        sx={{
                                            width: 34,
                                            height: 34,
                                            borderRadius: 1.5,
                                            display: 'grid',
                                            placeItems: 'center',
                                            bgcolor: data.openIncidents > 0
                                                ? alpha(theme.palette.error.main, 0.12)
                                                : alpha(theme.palette.success.main, 0.12),
                                            color: data.openIncidents > 0
                                                ? 'error.main'
                                                : 'success.main',
                                        }}
                                    >
                                        <NotificationsActiveIcon fontSize="small" />
                                    </Box>

                                    <Box>
                                        <Typography variant="h6" sx={{ fontWeight: 900 }}>
                                            Инциденты
                                        </Typography>

                                        <Typography color="text.secondary" sx={{ fontSize: 14 }}>
                                            Текущая аварийная активность.
                                        </Typography>
                                    </Box>
                                </Stack>

                                <Box
                                    sx={{
                                        borderRadius: 1.5,
                                        p: 2,
                                        bgcolor: data.openIncidents > 0
                                            ? alpha(theme.palette.error.main, 0.08)
                                            : alpha(theme.palette.success.main, 0.08),
                                    }}
                                >
                                    <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                        Открыто сейчас
                                    </Typography>

                                    <Typography variant="h3" sx={{ fontWeight: 900 }}>
                                        {data.openIncidents}
                                    </Typography>

                                    <Typography color="text.secondary" sx={{ fontSize: 14 }}>
                                        Закрыто всего: {data.resolvedIncidents}
                                    </Typography>
                                </Box>

                                <Box>
                                    <Typography color="text.secondary" sx={{ fontSize: 13 }}>
                                        Доля недоступных сервисов
                                    </Typography>

                                    <Typography variant="h5" sx={{ fontWeight: 900 }}>
                                        {unavailablePercent}%
                                    </Typography>
                                </Box>

                                <Button
                                    component={Link}
                                    to="/incidents"
                                    variant={data.openIncidents > 0 ? 'contained' : 'outlined'}
                                    color={data.openIncidents > 0 ? 'error' : 'primary'}
                                    endIcon={<ArrowForwardIcon />}
                                    fullWidth
                                >
                                    Журнал инцидентов
                                </Button>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Stack>
    );
}
