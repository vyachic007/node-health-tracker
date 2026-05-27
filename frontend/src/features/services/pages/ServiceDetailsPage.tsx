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
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { useMemo } from 'react';
import {
    CartesianGrid,
    Line,
    LineChart,
    ReferenceLine,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { servicesApi } from '../api/servicesApi';
import { HealthLevelChip } from '../components/HealthLevelChip';
import { ServiceDegradationAlert } from '../components/ServiceDegradationAlert';
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
    getSecondsUntil,
} from '../../../shared/lib/formatters';
import { useNow } from '../../../shared/lib/useNow';

export function ServiceDetailsPage() {
    const theme = useTheme();
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams();

    const now = useNow();
    const serviceId = Number(params.serviceId);

    const {
        data: service,
        isLoading: isServiceLoading,
        isError: isServiceError,
    } = useQuery({
        queryKey: ['services', serviceId],
        queryFn: () => servicesApi.getService(serviceId),
        enabled: Number.isFinite(serviceId),
        refetchInterval: 10000,
    });

    const {
        data: history = [],
        isLoading: isHistoryLoading,
    } = useQuery({
        queryKey: ['checks', serviceId, 'history'],
        queryFn: () => servicesApi.getCheckHistory(serviceId),
        enabled: Number.isFinite(serviceId),
        refetchInterval: 10000,
    });

    const secondsUntilNextCheck = useMemo(() => {
        if (service?.nextCheckAt) {
            return getSecondsUntil(service.nextCheckAt);
        }

        return service?.secondsUntilNextCheck ?? null;
    }, [service?.nextCheckAt, service?.secondsUntilNextCheck, now]);

    const runCheckMutation = useMutation({
        mutationFn: servicesApi.runCheck,
        onSuccess: () => {
            enqueueSnackbar('Проверка выполнена', { variant: 'success' });

            queryClient.invalidateQueries({ queryKey: ['services', serviceId] });
            queryClient.invalidateQueries({ queryKey: ['checks', serviceId, 'history'] });
            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['incidents', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['notifications', 'sent'] });
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

                    <Typography variant="h4">
                        {service.name}
                    </Typography>

                    <Typography color="text.secondary">
                        Детальная диагностика сервиса, история проверок, контроль деградации и текущий результат.
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
                    <Card
                        elevation={0}
                        sx={{
                            border: 1,
                            borderColor: service.degraded ? 'warning.main' : 'divider',
                            height: '100%',
                        }}
                    >
                        <CardContent>
                            <Stack spacing={2}>
                                <Stack
                                    direction="row"
                                    spacing={1}
                                    useFlexGap
                                    sx={{ flexWrap: 'wrap' }}
                                >
                                    <ServiceStatusChip status={service.lastStatus} />

                                    <HealthLevelChip level={service.healthLevel} />

                                    <Chip
                                        label={getCheckTypeLabel(service.checkType)}
                                        variant="outlined"
                                    />

                                    {service.degraded && (
                                        <Chip
                                            icon={<WarningAmberIcon />}
                                            label="Деградация"
                                            color="warning"
                                        />
                                    )}

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
                                        <Typography color="text.secondary">
                                            Адрес проверки
                                        </Typography>

                                        <Typography variant="h6">
                                            {service.targetHost}
                                            {service.port ? `:${service.port}` : ''}
                                            {service.path ?? ''}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            ID узла
                                        </Typography>

                                        <Typography variant="h6">
                                            {service.nodeId}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Последняя проверка
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatDateTime(service.lastCheckedAt)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Следующая проверка
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatSeconds(secondsUntilNextCheck)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Доступность за 24 часа
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatPercent(service.availabilityPercent24h)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Среднее время ответа за 24 часа
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatMilliseconds(service.averageResponseTimeMs24h)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Порог медленного ответа
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatMilliseconds(service.responseTimeThresholdMs)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">
                                            Медленных проверок подряд
                                        </Typography>

                                        <Typography variant="h6">
                                            {service.consecutiveDegradations} из {service.degradationThreshold}
                                        </Typography>
                                    </Grid>
                                </Grid>

                                <ServiceDegradationAlert
                                    lastStatus={service.lastStatus}
                                    lastResponseTimeMs={service.lastResponseTimeMs}
                                    responseTimeThresholdMs={service.responseTimeThresholdMs}
                                    degradationThreshold={service.degradationThreshold}
                                    consecutiveDegradations={service.consecutiveDegradations}
                                    degraded={service.degraded}
                                />

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
                                <Typography variant="h6">
                                    Оценка состояния
                                </Typography>

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
                                    <strong>
                                        {formatSeconds(service.currentDowntimeSeconds)}
                                    </strong>
                                </Typography>

                                <Divider />

                                <Typography variant="h6">
                                    Контроль деградации
                                </Typography>

                                <Alert severity={service.degraded ? 'warning' : 'success'}>
                                    {service.degraded
                                        ? 'Деградация подтверждена: сервис доступен, но несколько проверок подряд отвечает медленно.'
                                        : 'Деградация не подтверждена: сервис отвечает в пределах порога или медленных проверок подряд пока недостаточно.'}
                                </Alert>

                                <Typography>
                                    Порог ответа:{' '}
                                    <strong>
                                        {formatMilliseconds(service.responseTimeThresholdMs)}
                                    </strong>
                                </Typography>

                                <Typography>
                                    Медленных проверок подряд:{' '}
                                    <strong>
                                        {service.consecutiveDegradations}
                                    </strong>
                                </Typography>

                                <Typography>
                                    Нужно для подтверждения:{' '}
                                    <strong>
                                        {service.degradationThreshold}
                                    </strong>
                                </Typography>

                                <Typography color="text.secondary">
                                    Эта проверка нужна, чтобы видеть не только полный отказ сервиса, но и ухудшение качества работы до открытия критического инцидента.
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
                            <Typography variant="h6">
                                График времени ответа
                            </Typography>

                            <Typography color="text.secondary">
                                Последние результаты проверок сервиса. Пунктирная линия показывает порог медленного ответа.
                            </Typography>
                        </Box>

                        {isHistoryLoading ? (
                            <LinearProgress />
                        ) : chartData.length === 0 ? (
                            <Alert severity="info">
                                История проверок пока отсутствует.
                            </Alert>
                        ) : (
                            <div style={{ width: '100%', height: 320 }}>
                                <ResponsiveContainer>
                                    <LineChart data={chartData}>
                                        <CartesianGrid
                                            strokeDasharray="4 4"
                                            stroke={theme.palette.divider}
                                        />

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

                                        <ReferenceLine
                                            y={service.responseTimeThresholdMs}
                                            stroke={theme.palette.warning.main}
                                            strokeDasharray="6 6"
                                            label="Порог деградации"
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
                        <Typography variant="h6">
                            История проверок
                        </Typography>

                        {history.length === 0 ? (
                            <Alert severity="info">
                                История проверок пока отсутствует.
                            </Alert>
                        ) : (
                            history.slice(0, 10).map((item) => {
                                const isSlow =
                                    item.status === 'UP'
                                    && item.responseTimeMs !== null
                                    && item.responseTimeMs > service.responseTimeThresholdMs;

                                return (
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
                                            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                                                <Typography sx={{ fontWeight: 800 }}>
                                                    {item.status === 'UP' ? 'Работает' : 'Недоступен'}
                                                </Typography>

                                                {isSlow && (
                                                    <Chip
                                                        label="Медленный ответ"
                                                        color="warning"
                                                        size="small"
                                                    />
                                                )}
                                            </Stack>

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
                                );
                            })
                        )}
                    </Stack>
                </CardContent>
            </Card>
        </Stack>
    );
}