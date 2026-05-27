import {
    Alert,
    Avatar,
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
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import SpeedIcon from '@mui/icons-material/Speed';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import EmailIcon from '@mui/icons-material/Email';
import TelegramIcon from '@mui/icons-material/Telegram';
import ChatIcon from '@mui/icons-material/Chat';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { useEffect, useMemo, useState } from 'react';
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
import type { NetworkService } from '../model/serviceTypes';
import {
    formatDateTime,
    formatMilliseconds,
    formatPercent,
    formatSeconds,
    getSecondsUntil,
} from '../../../shared/lib/formatters';

function getServiceTarget(service: NetworkService): string {
    const port = service.port ? `:${service.port}` : '';
    const path = service.path ?? '';

    return `${service.targetHost}${port}${path}`;
}


function normalizeServiceHost(targetHost: string): string {
    return targetHost
        .replace(/^https?:\/\//, '')
        .split('/')[0]
        .split(':')[0]
        .trim()
        .toLowerCase();
}

function getServiceLogoUrl(service: NetworkService): string | null {
    const host = normalizeServiceHost(service.targetHost);
    const source = `${service.name} ${host}`.toLowerCase();

    if (source.includes('rutube')) {
        return 'https://www.google.com/s2/favicons?domain=rutube.ru&sz=64';
    }

    if (source.includes('gmail') || source.includes('google') || source.includes('smtp')) {
        return 'https://www.google.com/s2/favicons?domain=gmail.com&sz=64';
    }

    if (host.includes('.')) {
        return `https://www.google.com/s2/favicons?domain=${host}&sz=64`;
    }

    return null;
}

function getServiceLogo(service: NetworkService): string {
    const source = `${service.name} ${service.targetHost} ${service.checkType}`.toLowerCase();

    if (source.includes('rutube')) {
        return 'RT';
    }

    if (source.includes('gmail') || source.includes('mail') || source.includes('smtp')) {
        return 'GM';
    }

    if (source.includes('postgres') || source.includes('database') || source.includes('db')) {
        return 'DB';
    }

    if (source.includes('ssl')) {
        return 'SSL';
    }

    if (service.checkType === 'PING') {
        return 'PING';
    }

    if (service.checkType === 'TCP') {
        return 'TCP';
    }

    if (service.checkType === 'DNS') {
        return 'DNS';
    }

    if (service.checkType === 'HEARTBEAT') {
        return 'HB';
    }

    return 'WEB';
}

function getLogoColor(service: NetworkService) {
    if (service.lastStatus === 'DOWN') {
        return 'error.main';
    }

    if (service.degraded) {
        return 'warning.main';
    }

    return 'primary.main';
}

function formatDatePart(value: string | null): string {
    if (!value) {
        return '—';
    }

    return formatDateTime(value).split(',')[0] ?? formatDateTime(value);
}

function formatTimePart(value: string | null): string {
    if (!value) {
        return '';
    }

    return formatDateTime(value).split(',')[1]?.trim() ?? '';
}

function getHistoryStatusLabel(status: 'UP' | 'DOWN') {
    return status === 'UP' ? 'Работает' : 'Недоступен';
}

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

    const [secondsUntilNextCheck, setSecondsUntilNextCheck] = useState<number | null>(null);

    useEffect(() => {
        if (service?.secondsUntilNextCheck !== null && service?.secondsUntilNextCheck !== undefined) {
            setSecondsUntilNextCheck(Math.max(service.secondsUntilNextCheck, 0));
            return;
        }

        if (service?.nextCheckAt) {
            setSecondsUntilNextCheck(Math.max(getSecondsUntil(service.nextCheckAt) ?? 0, 0));
            return;
        }

        setSecondsUntilNextCheck(null);
    }, [service?.id, service?.secondsUntilNextCheck, service?.nextCheckAt]);

    useEffect(() => {
        const timerId = window.setInterval(() => {
            setSecondsUntilNextCheck((current) => {
                if (current === null) {
                    return null;
                }

                return Math.max(current - 1, 0);
            });
        }, 1000);

        return () => window.clearInterval(timerId);
    }, []);

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

    const chartData = useMemo(() => {
        return [...history]
            .reverse()
            .slice(-30)
            .map((item) => ({
                time: formatDateTime(item.checkedAt),
                responseTimeMs: item.responseTimeMs ?? 0,
                status: item.status === 'UP' ? 'Работает' : 'Недоступен',
            }));
    }, [history]);

    if (!Number.isFinite(serviceId)) {
        return <Alert severity="error">Некорректный ID сервиса.</Alert>;
    }

    if (isServiceLoading) {
        return <LinearProgress />;
    }

    if (isServiceError || !service) {
        return <Alert severity="error">Не удалось загрузить сервис.</Alert>;
    }

    const logo = getServiceLogo(service);
    const logoUrl = getServiceLogoUrl(service);
    const hasEnabledNotification =
        service.notifyEmail || service.notifyTelegram || service.notifyVk;

    const responseTimes = history
        .map((item) => item.responseTimeMs)
        .filter((value): value is number => value !== null);

    const minResponseTime = responseTimes.length > 0
        ? Math.min(...responseTimes)
        : null;

    const maxResponseTime = responseTimes.length > 0
        ? Math.max(...responseTimes)
        : null;

    return (
        <Stack spacing={3}>
            <Stack
                direction={{ xs: 'column', md: 'row' }}
                spacing={2}
                sx={{
                    justifyContent: 'space-between',
                    alignItems: { xs: 'stretch', md: 'center' },
                }}
            >
                <Button
                    component={Link}
                    to="/services"
                    startIcon={<ArrowBackIcon />}
                    sx={{ alignSelf: { xs: 'flex-start', md: 'center' } }}
                >
                    Назад к сервисам
                </Button>

                <Button
                    variant="contained"
                    startIcon={<PlayArrowIcon />}
                    onClick={() => runCheckMutation.mutate(service.id)}
                    disabled={runCheckMutation.isPending}
                    sx={{ minWidth: 190 }}
                >
                    {runCheckMutation.isPending ? 'Проверка...' : 'Проверить сейчас'}
                </Button>
            </Stack>

            <Card
                elevation={0}
                sx={{
                    border: 1,
                    borderColor: service.hasOpenIncident
                        ? 'error.main'
                        : service.degraded
                            ? 'warning.main'
                            : 'divider',
                    overflow: 'hidden',
                }}
            >
                <CardContent sx={{ p: 0 }}>
                    <Box sx={{ p: 3 }}>
                        <Stack
                            direction={{ xs: 'column', md: 'row' }}
                            spacing={3}
                            sx={{
                                justifyContent: 'space-between',
                                alignItems: { xs: 'flex-start', md: 'center' },
                            }}
                        >
                            <Stack direction="row" spacing={2} sx={{ alignItems: 'center', minWidth: 0 }}>
                                <Avatar
                                    src={logoUrl ?? undefined}
                                    variant="rounded"
                                    sx={{
                                        width: 72,
                                        height: 72,
                                        fontSize: 24,
                                        fontWeight: 900,
                                        bgcolor: getLogoColor(service),
                                    }}
                                >
                                    {logo}
                                </Avatar>

                                <Box sx={{ minWidth: 0 }}>
                                    <Stack
                                        direction="row"
                                        spacing={1}
                                        useFlexGap
                                        sx={{ flexWrap: 'wrap', mb: 1 }}
                                    >
                                        <Chip
                                            label={getCheckTypeLabel(service.checkType)}
                                            variant="outlined"
                                            size="small"
                                        />

                                        <ServiceStatusChip status={service.lastStatus} />

                                        <HealthLevelChip level={service.healthLevel} />

                                        {service.degraded && (
                                            <Chip
                                                icon={<WarningAmberIcon />}
                                                label="Деградация"
                                                color="warning"
                                                size="small"
                                            />
                                        )}

                                        {service.hasOpenIncident && (
                                            <Chip
                                                icon={<WarningAmberIcon />}
                                                label="Открыт инцидент"
                                                color="error"
                                                size="small"
                                                variant="outlined"
                                            />
                                        )}
                                    </Stack>

                                    <Typography variant="h4" noWrap>
                                        {service.name}
                                    </Typography>

                                    <Typography color="text.secondary" noWrap>
                                        {getServiceTarget(service)}
                                    </Typography>

                                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                                        Детальная диагностика, история проверок, деградация и текущий результат.
                                    </Typography>
                                </Box>
                            </Stack>

                            <Box sx={{ minWidth: 180 }}>
                                <Typography variant="h2" sx={{ lineHeight: 1, textAlign: { xs: 'left', md: 'right' } }}>
                                    {service.healthScore}
                                    <Typography component="span" variant="h6" color="text.secondary">
                                        /100
                                    </Typography>
                                </Typography>

                                <Typography color="text.secondary" sx={{ textAlign: { xs: 'left', md: 'right' } }}>
                                    оценка состояния
                                </Typography>

                                <LinearProgress
                                    variant="determinate"
                                    value={service.healthScore}
                                    color={
                                        service.healthScore >= 80
                                            ? 'success'
                                            : service.healthScore >= 50
                                                ? 'warning'
                                                : 'error'
                                    }
                                    sx={{
                                        mt: 1.5,
                                        height: 7,
                                        borderRadius: 999,
                                    }}
                                />
                            </Box>
                        </Stack>
                    </Box>

                    <Divider />

                    <Grid container>
                        <Grid size={{ xs: 12, sm: 6, lg: 2.4 }}>
                            <Box sx={{ p: 2.5 }}>
                                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                                    <CheckCircleIcon color="success" />

                                    <Box>
                                        <Typography variant="caption" color="text.secondary">
                                            Доступность за 24 часа
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatPercent(service.availabilityPercent24h)}
                                        </Typography>
                                    </Box>
                                </Stack>
                            </Box>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6, lg: 2.4 }}>
                            <Box sx={{ p: 2.5 }}>
                                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                                    <SpeedIcon color="primary" />

                                    <Box>
                                        <Typography variant="caption" color="text.secondary">
                                            Средний ответ
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatMilliseconds(service.averageResponseTimeMs24h)}
                                        </Typography>
                                    </Box>
                                </Stack>
                            </Box>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6, lg: 2.4 }}>
                            <Box sx={{ p: 2.5 }}>
                                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                                    <AccessTimeIcon color="primary" />

                                    <Box>
                                        <Typography variant="caption" color="text.secondary">
                                            Следующая проверка
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatSeconds(secondsUntilNextCheck)}
                                        </Typography>
                                    </Box>
                                </Stack>
                            </Box>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6, lg: 2.4 }}>
                            <Box sx={{ p: 2.5 }}>
                                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                                    <CalendarMonthIcon color="primary" />

                                    <Box>
                                        <Typography variant="caption" color="text.secondary">
                                            Последняя проверка
                                        </Typography>

                                        <Typography variant="h6">
                                            {formatDatePart(service.lastCheckedAt)}
                                        </Typography>

                                        <Typography variant="body2" color="text.secondary">
                                            {formatTimePart(service.lastCheckedAt)}
                                        </Typography>
                                    </Box>
                                </Stack>
                            </Box>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6, lg: 2.4 }}>
                            <Box sx={{ p: 2.5 }}>
                                <Typography variant="caption" color="text.secondary">
                                    Уведомления
                                </Typography>

                                <Stack direction="row" spacing={1} sx={{ mt: 0.75 }}>
                                    {service.notifyEmail && <EmailIcon color="success" fontSize="small" />}
                                    {service.notifyTelegram && <TelegramIcon color="success" fontSize="small" />}
                                    {service.notifyVk && <ChatIcon color="success" fontSize="small" />}
                                    {!hasEnabledNotification && (
                                        <Typography variant="body2" color="text.secondary">
                                            отключены
                                        </Typography>
                                    )}
                                </Stack>
                            </Box>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, lg: 8 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Typography variant="h6">
                                    Основные показатели
                                </Typography>

                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">ID узла</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>{service.nodeId}</Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Тип проверки</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {getCheckTypeLabel(service.checkType)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Интервал проверки</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {formatSeconds(service.intervalSeconds)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Порог медленного ответа</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {formatMilliseconds(service.responseTimeThresholdMs)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Медленных проверок подряд</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {service.consecutiveDegradations} из {service.degradationThreshold}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Текущий простой</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {formatSeconds(service.currentDowntimeSeconds)}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Уровень сбоя</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {service.lastFailureLayer
                                                ? failureLayerLabels[service.lastFailureLayer]
                                                : 'Не определён'}
                                        </Typography>
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <Typography color="text.secondary">Открытый инцидент</Typography>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            {service.hasOpenIncident ? 'Да' : 'Нет'}
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

                <Grid size={{ xs: 12, lg: 4 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Typography variant="h6">
                                    Контроль деградации
                                </Typography>

                                <Alert severity={service.degraded ? 'warning' : 'success'}>
                                    {service.degraded
                                        ? 'Деградация подтверждена: сервис доступен, но несколько проверок подряд отвечает медленно.'
                                        : 'Деградация не подтверждена: сервис отвечает в пределах порога или медленных проверок подряд пока недостаточно.'}
                                </Alert>

                                <Divider />

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

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, xl: 9 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Stack
                                    direction={{ xs: 'column', md: 'row' }}
                                    spacing={2}
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: { xs: 'flex-start', md: 'center' },
                                    }}
                                >
                                    <Box>
                                        <Typography variant="h6">
                                            График времени ответа
                                        </Typography>

                                        <Typography color="text.secondary">
                                            Последние результаты проверок. Пунктирная линия показывает порог медленного ответа.
                                        </Typography>
                                    </Box>

                                    <Stack direction="row" spacing={1}>
                                        <Chip
                                            label={`Минимум: ${formatMilliseconds(minResponseTime)}`}
                                            variant="outlined"
                                        />

                                        <Chip
                                            label={`Максимум: ${formatMilliseconds(maxResponseTime)}`}
                                            variant="outlined"
                                        />
                                    </Stack>
                                </Stack>

                                {isHistoryLoading ? (
                                    <LinearProgress />
                                ) : chartData.length === 0 ? (
                                    <Alert severity="info">
                                        История проверок пока отсутствует.
                                    </Alert>
                                ) : (
                                    <div style={{ width: '100%', height: 330 }}>
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
                </Grid>

                <Grid size={{ xs: 12, xl: 3 }}>
                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider', height: '100%' }}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Typography variant="h6">
                                    Параметры мониторинга
                                </Typography>

                                <Divider />

                                <Typography>
                                    Тип: <strong>{getCheckTypeLabel(service.checkType)}</strong>
                                </Typography>

                                <Typography>
                                    Адрес: <strong>{getServiceTarget(service)}</strong>
                                </Typography>

                                <Typography>
                                    Интервал: <strong>{formatSeconds(service.intervalSeconds)}</strong>
                                </Typography>

                                <Typography>
                                    Порог ответа: <strong>{formatMilliseconds(service.responseTimeThresholdMs)}</strong>
                                </Typography>

                                <Typography>
                                    Порог деградации: <strong>{service.degradationThreshold}</strong>
                                </Typography>

                                <Divider />

                                <Typography>
                                    Создан: <strong>{formatDateTime(service.createdAt)}</strong>
                                </Typography>

                                <Typography>
                                    Обновлён: <strong>{formatDateTime(service.updatedAt)}</strong>
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

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
                                                    {getHistoryStatusLabel(item.status)}
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

                                        <Box sx={{ minWidth: { xs: 'auto', md: 240 } }}>
                                            <Typography>
                                                Ответ: <strong>{formatMilliseconds(item.responseTimeMs)}</strong>
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
