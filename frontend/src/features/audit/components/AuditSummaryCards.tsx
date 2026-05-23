import {
    Card,
    CardContent,
    Grid,
    Stack,
    Typography,
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ErrorIcon from '@mui/icons-material/Error';
import type { AuditEvent } from '../model/auditTypes';

interface AuditSummaryCardsProps {
    events: AuditEvent[];
}

export function AuditSummaryCards({ events }: AuditSummaryCardsProps) {
    const successCount = events.filter((event) => event.severity === 'SUCCESS').length;
    const warningCount = events.filter((event) => event.severity === 'WARNING').length;
    const errorCount = events.filter((event) => event.severity === 'ERROR').length;

    const cards = [
        {
            title: 'Всего событий',
            value: events.length,
            icon: <HistoryIcon color="primary" />,
        },
        {
            title: 'Успешные действия',
            value: successCount,
            icon: <CheckCircleIcon color="success" />,
        },
        {
            title: 'Предупреждения',
            value: warningCount,
            icon: <WarningAmberIcon color="warning" />,
        },
        {
            title: 'Ошибки',
            value: errorCount,
            icon: <ErrorIcon color="error" />,
        },
    ];

    return (
        <Grid container spacing={2}>
            {cards.map((card) => (
                <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                    <Card elevation={0} sx={{ height: '100%' }}>
                        <CardContent>
                            <Stack
                                direction="row"
                                spacing={2}
                                sx={{
                                    justifyContent: 'space-between',
                                    alignItems: 'flex-start',
                                }}
                            >
                                <Stack spacing={1}>
                                    <Typography sx={{ fontWeight: 800 }}>
                                        {card.title}
                                    </Typography>

                                    <Typography variant="h4">
                                        {card.value}
                                    </Typography>
                                </Stack>

                                {card.icon}
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}