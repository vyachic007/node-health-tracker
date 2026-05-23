import {
    Alert,
    Box,
    Button,
    FormControl,
    Grid,
    InputLabel,
    LinearProgress,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { auditApi } from '../api/auditApi';
import { AuditEventsTable } from '../components/AuditEventsTable';
import { AuditSummaryCards } from '../components/AuditSummaryCards';
import type {
    AuditEvent,
    AuditEventType,
    AuditSeverity,
} from '../model/auditTypes';

type AuditSeverityFilter = 'ALL' | AuditSeverity;
type AuditEventTypeFilter = 'ALL' | AuditEventType;

const demoAuditEvents: AuditEvent[] = [
    {
        id: 1,
        eventType: 'USER_LOGIN',
        severity: 'SUCCESS',
        username: 'demo_user',
        userId: 1,
        entityType: 'USER',
        entityId: 1,
        message: 'Пользователь demo_user выполнил вход в систему.',
        ipAddress: '127.0.0.1',
        userAgent: 'Demo browser',
        createdAt: new Date().toISOString(),
    },
    {
        id: 2,
        eventType: 'SERVICE_CREATED',
        severity: 'SUCCESS',
        username: 'demo_user',
        userId: 1,
        entityType: 'SERVICE',
        entityId: 2,
        message: 'Создан сервис мониторинга RuTube HTTPS.',
        ipAddress: '127.0.0.1',
        userAgent: 'Demo browser',
        createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
    },
    {
        id: 3,
        eventType: 'INCIDENT_OPENED',
        severity: 'WARNING',
        username: 'SYSTEM',
        userId: null,
        entityType: 'INCIDENT',
        entityId: 1,
        message: 'Открыт инцидент по сервису Broken HTTP Service.',
        ipAddress: null,
        userAgent: null,
        createdAt: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
    },
    {
        id: 4,
        eventType: 'NOTIFICATION_SENT',
        severity: 'SUCCESS',
        username: 'SYSTEM',
        userId: null,
        entityType: 'NOTIFICATION',
        entityId: 1,
        message: 'Telegram-уведомление об открытии инцидента успешно отправлено.',
        ipAddress: null,
        userAgent: null,
        createdAt: new Date(Date.now() - 1000 * 60 * 11).toISOString(),
    },
    {
        id: 5,
        eventType: 'NOTIFICATION_FAILED',
        severity: 'ERROR',
        username: 'SYSTEM',
        userId: null,
        entityType: 'NOTIFICATION',
        entityId: 2,
        message: 'Ошибка отправки уведомления: Telegram API вернул ошибку авторизации.',
        ipAddress: null,
        userAgent: null,
        createdAt: new Date(Date.now() - 1000 * 60 * 20).toISOString(),
    },
];

function filterEvents(
    events: AuditEvent[],
    search: string,
    severityFilter: AuditSeverityFilter,
    eventTypeFilter: AuditEventTypeFilter,
) {
    const normalizedSearch = search.trim().toLowerCase();

    return events.filter((event) => {
        const matchesSeverity =
            severityFilter === 'ALL' || event.severity === severityFilter;

        const matchesEventType =
            eventTypeFilter === 'ALL' || event.eventType === eventTypeFilter;

        const searchableText = [
            event.message,
            event.username,
            event.entityType,
            event.entityId?.toString(),
            event.ipAddress,
            event.eventType,
            event.severity,
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();

        const matchesSearch =
            !normalizedSearch || searchableText.includes(normalizedSearch);

        return matchesSeverity && matchesEventType && matchesSearch;
    });
}

export function AuditPage() {
    const [search, setSearch] = useState('');
    const [severityFilter, setSeverityFilter] = useState<AuditSeverityFilter>('ALL');
    const [eventTypeFilter, setEventTypeFilter] = useState<AuditEventTypeFilter>('ALL');

    const {
        data: apiEvents = [],
        isLoading,
        isError,
        refetch,
        isFetching,
    } = useQuery({
        queryKey: ['audit', 'events'],
        queryFn: auditApi.getEvents,
        retry: false,
    });

    const isDemoMode = isError;
    const events = isDemoMode ? demoAuditEvents : apiEvents;

    const sortedEvents = useMemo(() => {
        return [...events].sort((a, b) => {
            const aTime = new Date(a.createdAt).getTime();
            const bTime = new Date(b.createdAt).getTime();

            return bTime - aTime;
        });
    }, [events]);

    const visibleEvents = useMemo(() => {
        return filterEvents(
            sortedEvents,
            search,
            severityFilter,
            eventTypeFilter,
        );
    }, [sortedEvents, search, severityFilter, eventTypeFilter]);

    const eventTypes = useMemo(() => {
        return Array.from(new Set(events.map((event) => event.eventType))).sort();
    }, [events]);

    if (isLoading) {
        return <LinearProgress />;
    }

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
                    <Typography variant="h4">
                        Аудит
                    </Typography>

                    <Typography color="text.secondary">
                        Журнал действий пользователя, изменений узлов, сервисов, инцидентов и уведомлений.
                    </Typography>
                </Box>

                <Button
                    variant="outlined"
                    startIcon={<RefreshIcon />}
                    onClick={() => refetch()}
                    disabled={isFetching}
                >
                    {isFetching ? 'Обновление...' : 'Обновить'}
                </Button>
            </Stack>

            {isDemoMode && (
                <Alert severity="warning">
                    Backend endpoint /api/audit/events пока не реализован. Сейчас показаны демонстрационные события для проверки интерфейса.
                </Alert>
            )}

            <AuditSummaryCards events={events} />

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                        label="Поиск"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                        placeholder="Сообщение, пользователь, объект, IP"
                        fullWidth
                    />
                </Grid>

                <Grid size={{ xs: 12, md: 3 }}>
                    <FormControl fullWidth>
                        <InputLabel>Статус</InputLabel>

                        <Select
                            label="Статус"
                            value={severityFilter}
                            onChange={(event) =>
                                setSeverityFilter(event.target.value as AuditSeverityFilter)
                            }
                        >
                            <MenuItem value="ALL">Все статусы</MenuItem>
                            <MenuItem value="INFO">Информация</MenuItem>
                            <MenuItem value="SUCCESS">Успешно</MenuItem>
                            <MenuItem value="WARNING">Предупреждения</MenuItem>
                            <MenuItem value="ERROR">Ошибки</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>

                <Grid size={{ xs: 12, md: 3 }}>
                    <FormControl fullWidth>
                        <InputLabel>Тип события</InputLabel>

                        <Select
                            label="Тип события"
                            value={eventTypeFilter}
                            onChange={(event) =>
                                setEventTypeFilter(event.target.value as AuditEventTypeFilter)
                            }
                        >
                            <MenuItem value="ALL">Все события</MenuItem>

                            {eventTypes.map((eventType) => (
                                <MenuItem key={eventType} value={eventType}>
                                    {eventType}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Grid>
            </Grid>

            <Typography color="text.secondary">
                Показано событий: {visibleEvents.length} из {events.length}
            </Typography>

            <AuditEventsTable events={visibleEvents} />
        </Stack>
    );
}