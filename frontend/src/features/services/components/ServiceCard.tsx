import {
    Avatar,
    Box,
    Chip,
    IconButton,
    LinearProgress,
    Menu,
    MenuItem,
    Stack,
    TableCell,
    TableRow,
    Tooltip,
    Typography,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import InfoIcon from '@mui/icons-material/Info';
import SpeedIcon from '@mui/icons-material/Speed';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import { useEffect, useMemo, useState } from 'react';
import type { MouseEvent } from 'react';
import { Link } from 'react-router-dom';
import {
    formatDateTime,
    formatMilliseconds,
    formatPercent,
    formatSeconds,
    getSecondsUntil,
} from '../../../shared/lib/formatters';
import { getCheckTypeLabel } from '../model/serviceLabels';
import type { NetworkService } from '../model/serviceTypes';

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

function getStatusIcon(service: NetworkService) {
    if (!service.lastStatus) {
        return <RadioButtonUncheckedIcon fontSize="small" />;
    }

    if (service.lastStatus === 'UP') {
        return <CheckCircleIcon fontSize="small" />;
    }

    return <ErrorIcon fontSize="small" />;
}

function getStatusLabel(service: NetworkService): string {
    if (!service.lastStatus) {
        return 'Не проверялся';
    }

    if (!service.isEnabled) {
        return 'Отключён';
    }

    if (service.lastStatus === 'UP' && service.degraded) {
        return 'С деградацией';
    }

    if (service.lastStatus === 'UP') {
        return 'Работает';
    }

    return 'Недоступен';
}

function getStatusColor(service: NetworkService): 'success' | 'warning' | 'error' | 'default' {
    if (!service.lastStatus || !service.isEnabled) {
        return 'default';
    }

    if (service.lastStatus === 'UP' && service.degraded) {
        return 'warning';
    }

    if (service.lastStatus === 'UP') {
        return 'success';
    }

    return 'error';
}

function getLogoColor(service: NetworkService): 'primary.main' | 'success.main' | 'warning.main' | 'error.main' | 'grey.500' {
    if (!service.lastStatus || !service.isEnabled) {
        return 'grey.500';
    }

    if (service.lastStatus === 'DOWN') {
        return 'error.main';
    }

    if (service.degraded) {
        return 'warning.main';
    }

    return 'primary.main';
}

function getAvailabilityProgress(value: number | null): number {
    if (value === null || value === undefined || Number.isNaN(value)) {
        return 0;
    }

    return Math.max(0, Math.min(value, 100));
}

function getAvailabilityColor(value: number | null): 'success' | 'warning' | 'error' | 'inherit' {
    if (value === null || value === undefined) {
        return 'inherit';
    }

    if (value >= 95) {
        return 'success';
    }

    if (value >= 70) {
        return 'warning';
    }

    return 'error';
}

export function ServiceCard({
                                service,
                                isChecking,
                                isDeleting,
                                onRunCheck,
                                onEdit,
                                onDelete,
                            }: ServiceCardProps) {
    const [secondsUntilNextCheck, setSecondsUntilNextCheck] = useState<number | null>(null);
    const [menuAnchorEl, setMenuAnchorEl] = useState<null | HTMLElement>(null);

    const logo = useMemo(() => getServiceLogo(service), [service]);

    const availabilityProgress = getAvailabilityProgress(service.availabilityPercent24h);
    const availabilityColor = getAvailabilityColor(service.availabilityPercent24h);

    useEffect(() => {
        if (service.secondsUntilNextCheck !== null && service.secondsUntilNextCheck !== undefined) {
            setSecondsUntilNextCheck(Math.max(service.secondsUntilNextCheck, 0));
            return;
        }

        if (service.nextCheckAt) {
            setSecondsUntilNextCheck(Math.max(getSecondsUntil(service.nextCheckAt) ?? 0, 0));
            return;
        }

        setSecondsUntilNextCheck(null);
    }, [service.id, service.secondsUntilNextCheck, service.nextCheckAt]);

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

    const handleMenuOpen = (event: MouseEvent<HTMLElement>) => {
        setMenuAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setMenuAnchorEl(null);
    };

    const handleEdit = () => {
        handleMenuClose();
        onEdit(service);
    };

    const handleDelete = () => {
        handleMenuClose();
        onDelete(service);
    };

    return (
        <TableRow
            hover
            sx={{
                '& td': {
                    borderBottomColor: 'divider',
                    py: 1.55,
                },
                ...(service.hasOpenIncident && {
                    '& td:first-of-type': {
                        borderLeft: 3,
                        borderLeftColor: 'error.main',
                    },
                    '& td': {
                        backgroundColor: 'rgba(211, 47, 47, 0.018)',
                    },
                }),
            }}
        >
            <TableCell>
                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 280 }}>
                    <Avatar
                        variant="rounded"
                        sx={{
                            width: 38,
                            height: 38,
                            fontSize: 12,
                            fontWeight: 900,
                            bgcolor: getLogoColor(service),
                        }}
                    >
                        {logo}
                    </Avatar>

                    <Box sx={{ minWidth: 0 }}>
                        <Typography sx={{ fontWeight: 900 }} noWrap>
                            {service.name}
                        </Typography>

                        <Typography variant="body2" color="text.secondary" noWrap>
                            {getServiceTarget(service)}
                        </Typography>
                    </Box>
                </Stack>
            </TableCell>

            <TableCell>
                <Chip
                    label={getCheckTypeLabel(service.checkType)}
                    size="small"
                    variant="outlined"
                />
            </TableCell>

            <TableCell>
                <Stack spacing={0.7} sx={{ alignItems: 'flex-start' }}>
                    <Chip
                        icon={getStatusIcon(service)}
                        label={getStatusLabel(service)}
                        color={getStatusColor(service)}
                        size="small"
                    />

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
            </TableCell>

            <TableCell sx={{ minWidth: 170 }}>
                <Stack spacing={0.75}>
                    <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
                        <Typography variant="body2" sx={{ fontWeight: 800 }}>
                            {formatPercent(service.availabilityPercent24h)}
                        </Typography>

                        <Typography variant="caption" color="text.secondary">
                            24 часа
                        </Typography>
                    </Stack>

                    <LinearProgress
                        variant="determinate"
                        value={availabilityProgress}
                        color={availabilityColor}
                        sx={{
                            height: 7,
                            borderRadius: 999,
                            bgcolor: 'action.hover',
                        }}
                    />
                </Stack>
            </TableCell>

            <TableCell>
                <Typography sx={{ fontWeight: 800 }}>
                    {formatMilliseconds(service.lastResponseTimeMs)}
                </Typography>

                {service.degraded && (
                    <Typography variant="caption" color="warning.main">
                        медленно: {service.consecutiveDegradations}/{service.degradationThreshold}
                    </Typography>
                )}
            </TableCell>

            <TableCell>
                <Typography sx={{ fontWeight: 800 }}>
                    {formatSeconds(secondsUntilNextCheck)}
                </Typography>
            </TableCell>

            <TableCell>
                <Typography sx={{ fontWeight: 800 }} noWrap>
                    {formatDateTime(service.lastCheckedAt)}
                </Typography>

                <Typography variant="caption" color="text.secondary">
                    score: {service.healthScore}/100
                </Typography>
            </TableCell>

            <TableCell align="right">
                <Stack direction="row" spacing={0.75} sx={{ justifyContent: 'flex-end' }}>
                    <Tooltip title={isChecking ? 'Проверка выполняется' : 'Проверить сейчас'}>
                        <span>
                            <IconButton
                                color="primary"
                                onClick={() => onRunCheck(service.id)}
                                disabled={isChecking || isDeleting}
                                size="small"
                            >
                                <SpeedIcon />
                            </IconButton>
                        </span>
                    </Tooltip>

                    <Tooltip title="Подробнее">
                        <span>
                            <IconButton
                                component={Link}
                                to={`/services/${service.id}`}
                                color="primary"
                                disabled={isDeleting}
                                size="small"
                                sx={{
                                    border: 1,
                                    borderColor: 'primary.main',
                                }}
                            >
                                <InfoIcon />
                            </IconButton>
                        </span>
                    </Tooltip>

                    <Tooltip title="Действия">
                        <span>
                            <IconButton
                                size="small"
                                onClick={handleMenuOpen}
                                disabled={isChecking || isDeleting}
                            >
                                <MoreVertIcon />
                            </IconButton>
                        </span>
                    </Tooltip>
                </Stack>

                <Menu
                    anchorEl={menuAnchorEl}
                    open={Boolean(menuAnchorEl)}
                    onClose={handleMenuClose}
                    anchorOrigin={{
                        vertical: 'bottom',
                        horizontal: 'right',
                    }}
                    transformOrigin={{
                        vertical: 'top',
                        horizontal: 'right',
                    }}
                >
                    <MenuItem onClick={handleEdit}>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            <EditIcon fontSize="small" />
                            <span>Редактировать</span>
                        </Stack>
                    </MenuItem>

                    <MenuItem onClick={handleDelete} sx={{ color: 'error.main' }}>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            <DeleteIcon fontSize="small" />
                            <span>Удалить</span>
                        </Stack>
                    </MenuItem>
                </Menu>
            </TableCell>
        </TableRow>
    );
}
