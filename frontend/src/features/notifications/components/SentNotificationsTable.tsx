import {
    Alert,
    Card,
    CardContent,
    Chip,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
} from '@mui/material';
import { formatDateTime } from '../../../shared/lib/formatters';
import {
    getNotificationEventLabel,
    getSentStatusColor,
    getSentStatusLabel,
    notificationChannelLabels,
} from '../model/notificationLabels';
import type { SentNotification } from '../model/notificationTypes';

interface SentNotificationsTableProps {
    notifications: SentNotification[];
}

export function SentNotificationsTable({ notifications }: SentNotificationsTableProps) {
    if (notifications.length === 0) {
        return (
            <Alert severity="info">
                История отправленных уведомлений пока пустая.
            </Alert>
        );
    }

    return (
        <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
            <CardContent>
                <Stack spacing={2}>
                    <Typography variant="h6">
                        История отправленных уведомлений
                    </Typography>

                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Дата</TableCell>
                                <TableCell>Канал</TableCell>
                                <TableCell>Событие</TableCell>
                                <TableCell>Инцидент</TableCell>
                                <TableCell>Статус</TableCell>
                                <TableCell>Ошибка</TableCell>
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {notifications.map((notification) => (
                                <TableRow key={notification.id}>
                                    <TableCell>
                                        {formatDateTime(notification.sentAt)}
                                    </TableCell>

                                    <TableCell>
                                        {notificationChannelLabels[notification.channel]}
                                    </TableCell>

                                    <TableCell>
                                        {getNotificationEventLabel(notification.event)}
                                    </TableCell>

                                    <TableCell>
                                        №{notification.incidentId}
                                    </TableCell>

                                    <TableCell>
                                        <Chip
                                            label={getSentStatusLabel(notification.status)}
                                            color={getSentStatusColor(notification.status)}
                                            size="small"
                                        />
                                    </TableCell>

                                    <TableCell>
                                        {notification.errorMessage ?? '—'}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Stack>
            </CardContent>
        </Card>
    );
}