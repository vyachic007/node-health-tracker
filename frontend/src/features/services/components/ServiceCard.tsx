import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Grid,
    IconButton,
    Stack,
    Tooltip,
    Typography,
} from '@mui/material';
import SpeedIcon from '@mui/icons-material/Speed';
import InfoIcon from '@mui/icons-material/Info';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Link } from 'react-router-dom';
import {
    formatDateTime,
    formatMilliseconds,
    formatPercent,
    formatSeconds,
} from '../../../shared/lib/formatters';
import {
    failureLayerLabels,
    getCheckTypeLabel,
} from '../model/serviceLabels';
import type { NetworkService } from '../model/serviceTypes';
import { HealthLevelChip } from './HealthLevelChip';
import { ServiceStatusChip } from './ServiceStatusChip';

interface ServiceCardProps {
    service: NetworkService;
    isChecking: boolean;
    isDeleting: boolean;
    onRunCheck: (serviceId: number) => void;
    onEdit: (service: NetworkService) => void;
    onDelete: (service: NetworkService) => void;
}

function getServiceTarget(service: NetworkService): string {
    const port = service.port ? `:${service.port}` : '';
    const path = service.path ?? '';

    return `${service.targetHost}${port}${path}`;
}

export function ServiceCard({
                                service,
                                isChecking,
                                isDeleting,
                                onRunCheck,
                                onEdit,
                                onDelete,
                            }: ServiceCardProps) {
    return (
        <Card
            elevation={0}
            sx={{
                height: '100%',
                border: 1,
                borderColor: service.hasOpenIncident ? 'error.main' : 'divider',
            }}
        >
            <CardContent>
                <Stack spacing={2.25}>
                    <Stack
                        direction="row"
                        spacing={2}
                        sx={{ justifyContent: 'space-between' }}
                    >
                        <Box sx={{ minWidth: 0 }}>
                            <Typography variant="h6" noWrap>
                                {service.name}
                            </Typography>

                            <Typography variant="body2" color="text.secondary" noWrap>
                                Цель проверки: {getServiceTarget(service)}
                            </Typography>
                        </Box>

                        <ServiceStatusChip status={service.lastStatus} />
                    </Stack>

                    <Stack
                        direction="row"
                        spacing={1}
                        useFlexGap
                        sx={{ flexWrap: 'wrap' }}
                    >
                        <Chip
                            label={getCheckTypeLabel(service.checkType)}
                            size="small"
                            variant="outlined"
                        />

                        <HealthLevelChip level={service.healthLevel} />

                        {!service.isEnabled && (
                            <Chip
                                label="Отключён"
                                color="default"
                                size="small"
                            />
                        )}

                        {service.hasOpenIncident && (
                            <Chip
                                icon={<WarningAmberIcon />}
                                label={`Инцидент №${service.openIncidentId}`}
                                color="error"
                                size="small"
                            />
                        )}
                    </Stack>

                    <Grid container spacing={2}>
                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Оценка здоровья
                            </Typography>

                            <Typography variant="h5">
                                {service.healthScore}/100
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Последний ответ
                            </Typography>

                            <Typography variant="h5">
                                {formatMilliseconds(service.lastResponseTimeMs)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Доступность за 24 часа
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatPercent(service.availabilityPercent24h)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Следующая проверка
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatSeconds(service.secondsUntilNextCheck)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Интервал
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatSeconds(service.intervalSeconds)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Последняя проверка
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatDateTime(service.lastCheckedAt)}
                            </Typography>
                        </Grid>

                        <Grid size={12}>
                            <Typography variant="caption" color="text.secondary">
                                Уровень сбоя
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {service.lastFailureLayer
                                    ? failureLayerLabels[service.lastFailureLayer]
                                    : '—'}
                            </Typography>
                        </Grid>
                    </Grid>

                    {service.lastDiagnosticMessage && (
                        <Alert severity={service.lastStatus === 'DOWN' ? 'error' : 'success'}>
                            {service.lastDiagnosticMessage}
                        </Alert>
                    )}

                    {service.lastRecommendation && service.lastStatus === 'DOWN' && (
                        <Typography variant="body2" color="text.secondary">
                            Рекомендация: {service.lastRecommendation}
                        </Typography>
                    )}

                    <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                        <Tooltip title={isChecking ? 'Проверка выполняется' : 'Проверить сервис сейчас'}>
                            <span>
                                <IconButton
                                    color="primary"
                                    onClick={() => onRunCheck(service.id)}
                                    disabled={isChecking || isDeleting}
                                    sx={{
                                        width: 42,
                                        height: 42,
                                        border: 1,
                                        borderColor: 'primary.main',
                                    }}
                                >
                                    <SpeedIcon />
                                </IconButton>
                            </span>
                        </Tooltip>

                        <Tooltip title="Открыть подробную информацию">
                            <span style={{ flex: 1 }}>
                                <Button
                                    component={Link}
                                    to={`/services/${service.id}`}
                                    variant="outlined"
                                    startIcon={<InfoIcon />}
                                    disabled={isDeleting}
                                    fullWidth
                                >
                                    Подробнее
                                </Button>
                            </span>
                        </Tooltip>

                        <Tooltip title="Редактировать сервис">
                            <span>
                                <IconButton
                                    color="default"
                                    onClick={() => onEdit(service)}
                                    disabled={isChecking || isDeleting}
                                    sx={{
                                        width: 42,
                                        height: 42,
                                        border: 1,
                                        borderColor: 'divider',
                                    }}
                                >
                                    <EditIcon />
                                </IconButton>
                            </span>
                        </Tooltip>

                        <Tooltip title="Удалить сервис">
                            <span>
                                <IconButton
                                    color="error"
                                    onClick={() => onDelete(service)}
                                    disabled={isChecking || isDeleting}
                                    sx={{
                                        width: 42,
                                        height: 42,
                                        border: 1,
                                        borderColor: 'error.main',
                                    }}
                                >
                                    <DeleteIcon />
                                </IconButton>
                            </span>
                        </Tooltip>
                    </Stack>
                </Stack>
            </CardContent>
        </Card>
    );
}