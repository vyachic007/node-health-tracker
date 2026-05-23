import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Divider,
    Grid,
    LinearProgress,
    Stack,
    Typography,
    useTheme,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import {
    CartesianGrid,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { servicesApi } from '../api/servicesApi';
import { HealthLevelChip } from '../components/HealthLevelChip';
import { ServiceStatusChip } from '../components/ServiceStatusChip';
import {
    failureLayerLabels,
    getCheckTypeLabel,
} from '../model/serviceLabels';
import {
    formatDateTime,
    formatMilliseconds,
    formatPercent,
    formatSeconds,
} from '../../../shared/lib/formatters';

export function ServiceDetailsPage() {
    const theme = useTheme();
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams();

    const serviceId = Number(params.serviceId);

    const {
        data: service,
        isLoading: isServiceLoading,
        isError: isServiceError,
    } = useQuery({
        queryKey: ['services', serviceId],
        queryFn: () => servicesApi.getService(serviceId),
        enabled: Number.isFinite(serviceId),
    });

    const {
        data: history = [],
        isLoading: isHistoryLoading,
    } = useQuery({
        queryKey: ['checks', serviceId, 'history'],
        queryFn: () => servicesApi.getCheckHistory(serviceId),
        enabled: Number.isFinite(serviceId),
    });

    const runCheckMutation = useMutation({
        mutationFn: servicesApi.runCheck,
        onSuccess: () => {
            enqueueSnackbar('Проверка выполнена', { variant: 'success' });
            queryClient.invalidateQueries({ queryKey: ['services', serviceId] });
            queryClient.invalidateQueries({ queryKey: ['checks', serviceId, 'history'] });
            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось выполнить проверку', { variant: 'error' });
        },
    });

    if (!Number.isFinite(serviceId)) {
        return <Alert severity="error">Некорректный ID сервиса.</Alert>;
    }

    if (isServiceLoading) {
        return <LinearProgress />;
    }

    if (isServiceError || !service) {
        return <Alert severity="error">Не удалось загрузить сервис.</Alert>;
    }

    const chartData = [...history]
        .reverse()
        .slice(-30)
        .map((item) => ({
            time: formatDateTime(item.checkedAt),
            responseTimeMs: item.responseTimeMs ?? 0,
            status: item.status === 'UP' ? 'Работает' : 'Недоступен',
        }));

    return (
        <Stack spacing={3}>
            <Stack
                direction={{ xs: 'column', md: 'row' }}
                spacing={2}
                sx={{
                    justifyContent: 'space-between',
                    alignItems: { xs: 'stretch', md: 'flex-start' },
                }}
            >
                <Box>
                    <Button
                        component={Link}
                        to="/services"
                        startIcon={<ArrowBackIcon />}
                        sx={{ mb: 1 }}
                    >
                        Назад к сервисам
                    </Button>

                    <Typography variant="h4">{service.name}</Typography>

                    <Typography color="text.secondary">
                        Детальная диагностика сервиса, история проверок и текущий результат.
                    </Typography>
                </Box>

                <Button
                    variant="contained"
                    startIcon={<PlayArrowIcon />}
                    onClick={() => runCheckMutation.mutate(service.id)}
                    disabled={runCheckMutation.isPending}
                >
                    {runCheckMutation.isPending ? 'Проверка...' : 'Проверить сейчас'}
                </Button>
            </Stack>

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 8 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                                    <ServiceStatusChip status={service.lastStatus} />
                                    <HealthLevelChip level={service.healthLevel} />
                                    <Chip label={getCheckTypeLabel(service.checkType)} variant="outlined" />
                                    {service.hasOpenIncident && (
                                        <Chip
                                            label={`Открытый инцидент №${service.openIncidentId}`}
                                            color="error"
                                        />
                                    )}
                                </Stack>

                                <Divider />

                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Адрес проверки</Typography>
                                        <Typography variant="h6">
                                            {service.targetHost}
                                            {service.port ? `:${service.port}` : ''}
                                            {service.path ?? ''}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">ID узла</Typography>
                                        <Typography variant="h6">{service.nodeId}</Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Последняя проверка</Typography>
                                        <Typography variant="h6">{formatDateTime(service.lastCheckedAt)}</Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Следующая проверка</Typography>
                                        <Typography variant="h6">
                                            {formatSeconds(service.secondsUntilNextCheck)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Доступность за 24 часа</Typography>
                                        <Typography variant="h6">
                                            {formatPercent(service.availabilityPercent24h)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Среднее время ответа за 24 часа</Typography>
                                        <Typography variant="h6">
                                            {formatMilliseconds(service.averageResponseTimeMs24h)}
                                        </Typography>
                                    </Grid>
                                </Grid>

                                {service.lastDiagnosticMessage && (
                                    <Alert severity={service.lastStatus === 'DOWN' ? 'error' : 'success'}>
                                        {service.lastDiagnosticMessage}
                                    </Alert>
                                )}

                                {service.lastRecommendation && (
                                    <Alert severity="info">
                                        {service.lastRecommendation}
                                    </Alert>
                                )}
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 4 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Typography variant="h6">Оценка состояния</Typography>

                                <Typography variant="h2">
                                    {service.healthScore}
                                </Typography>

                                <Typography color="text.secondary">
                                    из 100
                                </Typography>

                                <Divider />

                                <Typography>
                                    Уровень сбоя:{' '}
                                    <strong>
                                        {service.lastFailureLayer
                                            ? failureLayerLabels[service.lastFailureLayer]
                                            : 'Не определён'}
                                    </strong>
                                </Typography>

                                <Typography>
                                    Текущий простой:{' '}
                                    <strong>{formatSeconds(service.currentDowntimeSeconds)}</strong>
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Stack spacing={2}>
                        <Box>
                            <Typography variant="h6">График времени ответа</Typography>
                            <Typography color="text.secondary">
                                Последние результаты проверок сервиса.
                            </Typography>
                        </Box>

                        {isHistoryLoading ? (
                            <LinearProgress />
                        ) : chartData.length === 0 ? (
                            <Alert severity="info">История проверок пока отсутствует.</Alert>
                        ) : (
                            <div style={{ width: '100%', height: 320 }}>
                                <ResponsiveContainer>
                                    <LineChart data={chartData}>
                                        <CartesianGrid strokeDasharray="4 4" stroke={theme.palette.divider} />
                                        <XAxis
                                            dataKey="time"
                                            stroke={theme.palette.text.secondary}
                                            tick={{ fontSize: 11 }}
                                        />
                                        <YAxis stroke={theme.palette.text.secondary} />
                                        <Tooltip
                                            contentStyle={{
                                                background: theme.palette.background.paper,
                                                border: `1px solid ${theme.palette.divider}`,
                                                borderRadius: 12,
                                            }}
                                        />
                                        <Line
                                            type="monotone"
                                            dataKey="responseTimeMs"
                                            name="Время ответа, мс"
                                            stroke={theme.palette.primary.main}
                                            strokeWidth={3}
                                            dot={false}
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        )}
                    </Stack>
                </CardContent>
            </Card>

            <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Stack spacing={2}>
                        <Typography variant="h6">История проверок</Typography>

                        {history.slice(0, 10).map((item) => (
                            <Stack
                                key={item.id}
                                direction={{ xs: 'column', md: 'row' }}
                                spacing={2}
                                sx={{
                                    justifyContent: 'space-between',
                                    borderBottom: 1,
                                    borderColor: 'divider',
                                    pb: 1.5,
                                }}
                            >
                                <Box>
                                    <Typography sx={{ fontWeight: 800 }}>
                                        {item.status === 'UP' ? 'Работает' : 'Недоступен'}
                                    </Typography>

                                    <Typography color="text.secondary">
                                        {formatDateTime(item.checkedAt)}
                                    </Typography>
                                </Box>

                                <Box>
                                    <Typography>
                                        Ответ: {formatMilliseconds(item.responseTimeMs)}
                                    </Typography>

                                    <Typography color="text.secondary">
                                        {item.diagnosticMessage}
                                    </Typography>
                                </Box>
                            </Stack>
                        ))}
                    </Stack>
                </CardContent>
            </Card>
        </Stack>
    );
}