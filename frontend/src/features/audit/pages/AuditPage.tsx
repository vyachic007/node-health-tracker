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
import { useAuth } from '../../auth/store/AuthContext';

type AuditSeverityFilter = 'ALL' | AuditSeverity;
type AuditEventTypeFilter = 'ALL' | AuditEventType;

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

function getEventTypeLabel(eventType: string) {
    switch (eventType) {
        case 'NODE_CREATED':
            return 'Узел создан';
        case 'NODE_UPDATED':
            return 'Узел обновлён';
        case 'NODE_DELETED':
            return 'Узел удалён';

        case 'SERVICE_CREATED':
            return 'Сервис создан';
        case 'SERVICE_UPDATED':
            return 'Сервис обновлён';
        case 'SERVICE_DELETED':
            return 'Сервис удалён';

        case 'CHECK_STARTED':
            return 'Проверка запущена';

        case 'INCIDENT_OPENED':
            return 'Инцидент открыт';
        case 'INCIDENT_RESOLVED':
            return 'Инцидент закрыт';

        case 'USER_BLOCKED':
            return 'Пользователь заблокирован';
        case 'USER_UNBLOCKED':
            return 'Пользователь разблокирован';
        case 'USER_ROLE_UPDATED':
            return 'Роль пользователя изменена';

        default:
            return eventType;
    }
}

export function AuditPage() {
    const { isAdmin } = useAuth();

    const [search, setSearch] = useState('');
    const [severityFilter, setSeverityFilter] = useState<AuditSeverityFilter>('ALL');
    const [eventTypeFilter, setEventTypeFilter] = useState<AuditEventTypeFilter>('ALL');

    const {
        data: events = [],
        isLoading,
        isError,
        refetch,
        isFetching,
    } = useQuery({
        queryKey: ['audit', 'events', isAdmin],
        queryFn: () => auditApi.getEvents(isAdmin),
        retry: false,
    });

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
                        Журнал действий пользователя, изменений узлов, сервисов и инцидентов.
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

            {isAdmin ? (
                <Alert severity="info">
                    Вы просматриваете общий журнал аудита по всем пользователям и системным событиям.
                </Alert>
            ) : (
                <Alert severity="info">
                    Вы просматриваете только свои действия. Системные события и действия других пользователей доступны администратору.
                </Alert>
            )}

            {isError && (
                <Alert severity="error">
                    Не удалось загрузить журнал аудита. Проверьте backend endpoint и права доступа.
                </Alert>
            )}

            <AuditSummaryCards events={events} />

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                        label="Поиск"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                        placeholder="Сообщение, пользователь, объект, тип события"
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
                                    {getEventTypeLabel(eventType)}
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