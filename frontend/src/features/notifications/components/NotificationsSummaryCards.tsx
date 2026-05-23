import { Card, CardContent, Grid, Stack, Typography } from '@mui/material';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import NotificationsOffIcon from '@mui/icons-material/NotificationsOff';
import SendIcon from '@mui/icons-material/Send';
import ErrorIcon from '@mui/icons-material/Error';
import type {
    NotificationSetting,
    SentNotification,
} from '../model/notificationTypes';

interface NotificationsSummaryCardsProps {
    settings: NotificationSetting[];
    sentNotifications: SentNotification[];
}

export function NotificationsSummaryCards({
                                              settings,
                                              sentNotifications,
                                          }: NotificationsSummaryCardsProps) {
    const activeSettings = settings.filter((setting) => setting.isEnabled).length;
    const disabledSettings = settings.filter((setting) => !setting.isEnabled).length;
    const sent = sentNotifications.filter((notification) => notification.status === 'SENT').length;
    const failed = sentNotifications.filter((notification) => notification.status === 'FAILED').length;

    const cards = [
        {
            title: 'Активные каналы',
            value: activeSettings,
            icon: <NotificationsActiveIcon />,
            color: 'success.main',
        },
        {
            title: 'Отключённые каналы',
            value: disabledSettings,
            icon: <NotificationsOffIcon />,
            color: 'text.secondary',
        },
        {
            title: 'Отправлено',
            value: sent,
            icon: <SendIcon />,
            color: 'primary.main',
        },
        {
            title: 'Ошибки отправки',
            value: failed,
            icon: <ErrorIcon />,
            color: 'error.main',
        },
    ];

    return (
        <Grid container spacing={2}>
            {cards.map((card) => (
                <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                    <Card elevation={0} sx={{ height: '100%', border: 1, borderColor: 'divider' }}>
                        <CardContent>
                            <Stack spacing={1.5}>
                                <Stack
                                    direction="row"
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                    }}
                                >
                                    <Typography color="text.secondary" sx={{ fontWeight: 700 }}>
                                        {card.title}
                                    </Typography>

                                    <Stack sx={{ color: card.color }}>
                                        {card.icon}
                                    </Stack>
                                </Stack>

                                <Typography variant="h4">
                                    {card.value}
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}