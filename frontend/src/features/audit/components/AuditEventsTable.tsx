import {
    Box,
    Card,
    CardContent,
    Chip,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Tooltip,
    Typography,
} from '@mui/material';
import type { AuditEvent } from '../model/auditTypes';
import {
    auditEventTypeLabels,
    auditSeverityLabels,
    getAuditSeverityColor,
} from '../model/auditLabels';
import { formatDateTime } from '../../../shared/lib/formatters';

interface AuditEventsTableProps {
    events: AuditEvent[];
}

function getEntityLabel(event: AuditEvent) {
    if (!event.entityType && !event.entityId) {
        return '—';
    }

    if (event.entityType && event.entityId) {
        return `${event.entityType} №${event.entityId}`;
    }

    return event.entityType ?? `№${event.entityId}`;
}

export function AuditEventsTable({ events }: AuditEventsTableProps) {
    if (events.length === 0) {
        return (
            <Card elevation={0}>
                <CardContent>
                    <Typography color="text.secondary">
                        События аудита не найдены.
                    </Typography>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card elevation={0}>
            <CardContent>
                <Stack spacing={2}>
                    <Typography variant="h5">
                        Журнал событий
                    </Typography>

                    <Box sx={{ overflowX: 'auto' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Дата</TableCell>
                                    <TableCell>Тип события</TableCell>
                                    <TableCell>Статус</TableCell>
                                    <TableCell>Пользователь</TableCell>
                                    <TableCell>Объект</TableCell>
                                    <TableCell>Описание</TableCell>
                                    <TableCell>IP</TableCell>
                                </TableRow>
                            </TableHead>

                            <TableBody>
                                {events.map((event) => (
                                    <TableRow key={event.id} hover>
                                        <TableCell sx={{ whiteSpace: 'nowrap' }}>
                                            {formatDateTime(event.createdAt)}
                                        </TableCell>

                                        <TableCell>
                                            {auditEventTypeLabels[event.eventType] ?? event.eventType}
                                        </TableCell>

                                        <TableCell>
                                            <Chip
                                                label={auditSeverityLabels[event.severity]}
                                                color={getAuditSeverityColor(event.severity)}
                                                size="small"
                                            />
                                        </TableCell>

                                        <TableCell>
                                            {event.username ?? 'Система'}
                                        </TableCell>

                                        <TableCell>
                                            {getEntityLabel(event)}
                                        </TableCell>

                                        <TableCell>
                                            <Tooltip title={event.message}>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        maxWidth: 420,
                                                        overflow: 'hidden',
                                                        textOverflow: 'ellipsis',
                                                        whiteSpace: 'nowrap',
                                                    }}
                                                >
                                                    {event.message}
                                                </Typography>
                                            </Tooltip>
                                        </TableCell>

                                        <TableCell>
                                            {event.ipAddress ?? '—'}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </Box>
                </Stack>
            </CardContent>
        </Card>
    );
}