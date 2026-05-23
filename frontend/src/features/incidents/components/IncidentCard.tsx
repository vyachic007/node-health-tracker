import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    Stack,
    Typography,
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { formatDateTime, formatSeconds } from '../../../shared/lib/formatters';
import type { Incident } from '../model/incidentTypes';
import { IncidentStatusChip } from './IncidentStatusChip';
import { SeverityChip } from './SeverityChip';

interface IncidentCardProps {
    incident: Incident;
    onOpenDetails: (incident: Incident) => void;
}

function getIncidentDurationSeconds(incident: Incident): number | null {
    if (!incident.openedAt) {
        return null;
    }

    const start = new Date(incident.openedAt).getTime();
    const end = incident.closedAt ? new Date(incident.closedAt).getTime() : Date.now();

    if (Number.isNaN(start) || Number.isNaN(end)) {
        return null;
    }

    return Math.max(0, Math.floor((end - start) / 1000));
}

export function IncidentCard({ incident, onOpenDetails }: IncidentCardProps) {
    const durationSeconds = getIncidentDurationSeconds(incident);

    return (
        <Card
            elevation={0}
            sx={{
                height: '100%',
                border: 1,
                borderColor: incident.status === 'OPEN' ? 'error.main' : 'divider',
            }}
        >
            <CardContent>
                <Stack spacing={2}>
                    <Stack
                        direction="row"
                        spacing={2}
                        sx={{ justifyContent: 'space-between' }}
                    >
                        <Box sx={{ minWidth: 0 }}>
                            <Typography variant="h6" noWrap>
                                {incident.serviceName}
                            </Typography>

                            <Typography variant="body2" color="text.secondary">
                                Инцидент №{incident.id} · сервис №{incident.serviceId}
                            </Typography>
                        </Box>

                        <IncidentStatusChip status={incident.status} />
                    </Stack>

                    <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                        <SeverityChip severity={incident.severity} />

                        {durationSeconds !== null && (
                            <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
                                <AccessTimeIcon fontSize="small" color="disabled" />

                                <Typography variant="body2" color="text.secondary">
                                    {formatSeconds(durationSeconds)}
                                </Typography>
                            </Stack>
                        )}
                    </Stack>

                    <Alert severity={incident.status === 'OPEN' ? 'error' : 'success'}>
                        {incident.reason}
                    </Alert>

                    <Grid container spacing={2}>
                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Открыт
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatDateTime(incident.openedAt)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Закрыт
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatDateTime(incident.closedAt)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Проверка открытия
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {incident.openedByCheckResultId ?? '—'}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Проверка закрытия
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {incident.closedByCheckResultId ?? '—'}
                            </Typography>
                        </Grid>
                    </Grid>

                    <Button
                        variant="outlined"
                        startIcon={<InfoIcon />}
                        onClick={() => onOpenDetails(incident)}
                        fullWidth
                    >
                        Подробнее
                    </Button>
                </Stack>
            </CardContent>
        </Card>
    );
}