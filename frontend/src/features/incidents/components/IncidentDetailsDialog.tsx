import {
    Alert,
    Box,
    Card,
    CardContent,
    Chip,
    Dialog,
    DialogContent,
    DialogTitle,
    Divider,
    LinearProgress,
    Stack,
    Tab,
    Tabs,
    Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import TimelineIcon from '@mui/icons-material/Timeline';
import AssignmentIcon from '@mui/icons-material/Assignment';
import RepeatIcon from '@mui/icons-material/Repeat';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { formatDateTime, formatSeconds } from '../../../shared/lib/formatters';
import { incidentsApi } from '../api/incidentsApi';
import {
    failureLayerLabels,
    getTimelineEventLabel,
    recurrenceLevelLabels,
} from '../model/incidentLabels';
import type { Incident } from '../model/incidentTypes';
import { IncidentStatusChip } from './IncidentStatusChip';
import { SeverityChip } from './SeverityChip';

interface IncidentDetailsDialogProps {
    incident: Incident | null;
    open: boolean;
    onClose: () => void;
}

export function IncidentDetailsDialog({
                                          incident,
                                          open,
                                          onClose,
                                      }: IncidentDetailsDialogProps) {
    const [tab, setTab] = useState(0);
    const incidentId = incident?.id;

    const timelineQuery = useQuery({
        queryKey: ['incident', incidentId, 'timeline'],
        queryFn: () => incidentsApi.getIncidentTimeline(incidentId!),
        enabled: Boolean(incidentId && open),
    });

    const checklistQuery = useQuery({
        queryKey: ['incident', incidentId, 'checklist'],
        queryFn: () => incidentsApi.getRecoveryChecklist(incidentId!),
        enabled: Boolean(incidentId && open),
    });

    const reportQuery = useQuery({
        queryKey: ['incident', incidentId, 'report'],
        queryFn: () => incidentsApi.getIncidentReport(incidentId!),
        enabled: Boolean(incidentId && open),
    });

    const recurrenceQuery = useQuery({
        queryKey: ['incident', incidentId, 'recurrence'],
        queryFn: () => incidentsApi.getRecurrenceAnalysis(incidentId!),
        enabled: Boolean(incidentId && open),
    });

    if (!incident) {
        return null;
    }

    const isLoading =
        timelineQuery.isLoading ||
        checklistQuery.isLoading ||
        reportQuery.isLoading ||
        recurrenceQuery.isLoading;

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="lg">
            <DialogTitle>
                <Stack spacing={1}>
                    <Typography variant="h5">
                        Инцидент №{incident.id}
                    </Typography>

                    <Typography color="text.secondary">
                        {incident.serviceName}
                    </Typography>

                    <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                        <IncidentStatusChip status={incident.status} />
                        <SeverityChip severity={incident.severity} />
                    </Stack>
                </Stack>
            </DialogTitle>

            <DialogContent>
                <Stack spacing={3}>
                    {isLoading && <LinearProgress />}

                    <Alert severity={incident.status === 'OPEN' ? 'error' : 'success'}>
                        {incident.reason}
                    </Alert>

                    <Tabs
                        value={tab}
                        onChange={(_, nextTab) => setTab(nextTab)}
                        variant="scrollable"
                        scrollButtons="auto"
                    >
                        <Tab icon={<AssignmentIcon />} iconPosition="start" label="Отчёт" />
                        <Tab icon={<TimelineIcon />} iconPosition="start" label="История" />
                        <Tab icon={<CheckCircleIcon />} iconPosition="start" label="Чек-лист" />
                        <Tab icon={<RepeatIcon />} iconPosition="start" label="Повторяемость" />
                    </Tabs>

                    <Divider />

                    {tab === 0 && (
                        <Stack spacing={2}>
                            {reportQuery.data ? (
                                <>
                                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                                        <CardContent>
                                            <Stack spacing={1.5}>
                                                <Typography variant="h6">
                                                    Автоматический отчёт
                                                </Typography>

                                                <Typography>
                                                    {reportQuery.data.summary}
                                                </Typography>

                                                <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                                                    <Chip
                                                        label={`Уровень сбоя: ${failureLayerLabels[reportQuery.data.failureLayer]}`}
                                                        variant="outlined"
                                                    />

                                                    <Chip
                                                        label={`Длительность: ${formatSeconds(reportQuery.data.durationSeconds)}`}
                                                        variant="outlined"
                                                    />

                                                    <Chip
                                                        label={`Событий: ${reportQuery.data.timelineEventsCount}`}
                                                        variant="outlined"
                                                    />
                                                </Stack>
                                            </Stack>
                                        </CardContent>
                                    </Card>

                                    <Alert severity="info">
                                        Рекомендация: {reportQuery.data.recommendation}
                                    </Alert>

                                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                                        <CardContent>
                                            <Stack spacing={1}>
                                                <Typography color="text.secondary">
                                                    Открыт: {formatDateTime(reportQuery.data.openedAt)}
                                                </Typography>

                                                <Typography color="text.secondary">
                                                    Закрыт: {formatDateTime(reportQuery.data.closedAt)}
                                                </Typography>

                                                <Typography color="text.secondary">
                                                    ID проверки открытия: {reportQuery.data.openedByCheckResultId ?? '—'}
                                                </Typography>

                                                <Typography color="text.secondary">
                                                    ID проверки закрытия: {reportQuery.data.closedByCheckResultId ?? '—'}
                                                </Typography>
                                            </Stack>
                                        </CardContent>
                                    </Card>
                                </>
                            ) : (
                                <Alert severity="warning">
                                    Отчёт по инциденту пока не загружен.
                                </Alert>
                            )}
                        </Stack>
                    )}

                    {tab === 1 && (
                        <Stack spacing={2}>
                            {timelineQuery.data && timelineQuery.data.length > 0 ? (
                                timelineQuery.data.map((event) => (
                                    <Card
                                        key={event.id}
                                        elevation={0}
                                        sx={{ border: 1, borderColor: 'divider' }}
                                    >
                                        <CardContent>
                                            <Stack spacing={0.75}>
                                                <Typography sx={{ fontWeight: 800 }}>
                                                    {getTimelineEventLabel(event.eventType)}
                                                </Typography>

                                                <Typography color="text.secondary">
                                                    {event.message}
                                                </Typography>

                                                <Typography variant="caption" color="text.secondary">
                                                    {formatDateTime(event.createdAt)}
                                                </Typography>

                                                {event.checkResultId && (
                                                    <Typography variant="caption" color="text.secondary">
                                                        ID проверки: {event.checkResultId}
                                                    </Typography>
                                                )}
                                            </Stack>
                                        </CardContent>
                                    </Card>
                                ))
                            ) : (
                                <Alert severity="info">
                                    История событий пока пустая.
                                </Alert>
                            )}
                        </Stack>
                    )}

                    {tab === 2 && (
                        <Stack spacing={2}>
                            {checklistQuery.data ? (
                                <>
                                    <Alert severity="info">
                                        {checklistQuery.data.summary}
                                    </Alert>

                                    {checklistQuery.data.items.map((item) => (
                                        <Card
                                            key={item.stepNumber}
                                            elevation={0}
                                            sx={{
                                                border: 1,
                                                borderColor: item.isCritical ? 'error.main' : 'divider',
                                            }}
                                        >
                                            <CardContent>
                                                <Stack spacing={1}>
                                                    <Stack
                                                        direction="row"
                                                        spacing={1}
                                                        sx={{ alignItems: 'center' }}
                                                    >
                                                        <Typography sx={{ fontWeight: 900 }}>
                                                            Шаг {item.stepNumber}. {item.title}
                                                        </Typography>

                                                        {item.isCritical && (
                                                            <Chip
                                                                icon={<PriorityHighIcon />}
                                                                label="Критично"
                                                                color="error"
                                                                size="small"
                                                            />
                                                        )}
                                                    </Stack>

                                                    <Typography color="text.secondary">
                                                        {item.description}
                                                    </Typography>
                                                </Stack>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </>
                            ) : (
                                <Alert severity="warning">
                                    Чек-лист восстановления пока не загружен.
                                </Alert>
                            )}
                        </Stack>
                    )}

                    {tab === 3 && (
                        <Stack spacing={2}>
                            {recurrenceQuery.data ? (
                                <>
                                    <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                                        <CardContent>
                                            <Stack spacing={1.5}>
                                                <Typography variant="h6">
                                                    Анализ повторяемости
                                                </Typography>

                                                <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                                                    <Chip
                                                        label={`Уровень повторяемости: ${recurrenceLevelLabels[recurrenceQuery.data.recurrenceLevel]}`}
                                                        color={recurrenceQuery.data.isRecurring ? 'warning' : 'success'}
                                                        variant="outlined"
                                                    />

                                                    <Chip
                                                        label={recurrenceQuery.data.isRecurring ? 'Повторяющийся сбой' : 'Разовый сбой'}
                                                        color={recurrenceQuery.data.isRecurring ? 'warning' : 'success'}
                                                    />
                                                </Stack>

                                                <Box>
                                                    <Typography color="text.secondary">
                                                        Похожие за 24 часа: {recurrenceQuery.data.similarIncidentsLast24h}
                                                    </Typography>

                                                    <Typography color="text.secondary">
                                                        Похожие за 7 дней: {recurrenceQuery.data.similarIncidentsLast7d}
                                                    </Typography>

                                                    <Typography color="text.secondary">
                                                        Похожие за 30 дней: {recurrenceQuery.data.similarIncidentsLast30d}
                                                    </Typography>
                                                </Box>
                                            </Stack>
                                        </CardContent>
                                    </Card>

                                    <Alert severity="info">
                                        {recurrenceQuery.data.recommendation}
                                    </Alert>
                                </>
                            ) : (
                                <Alert severity="warning">
                                    Анализ повторяемости пока не загружен.
                                </Alert>
                            )}
                        </Stack>
                    )}
                </Stack>
            </DialogContent>
        </Dialog>
    );
}