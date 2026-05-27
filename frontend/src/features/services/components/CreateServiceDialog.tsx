import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    TextField,
} from '@mui/material';
import { useEffect, useState } from 'react';
import { getCheckTypeLabel } from '../model/serviceLabels';
import type {
    CheckType,
    CreateNetworkServiceRequest,
} from '../model/serviceTypes';

interface CreateServiceDialogProps {
    open: boolean;
    isSubmitting: boolean;
    initialNodeId?: number | null;
    onClose: () => void;
    onSubmit: (payload: CreateNetworkServiceRequest) => void;
}

interface FormSubmitEvent {
    preventDefault: () => void;
}

const DEFAULT_CHECK_TYPE: CheckType = 'HTTP';
const DEFAULT_INTERVAL_SECONDS = '3600';
const DEFAULT_RESPONSE_TIME_THRESHOLD_MS = '1000';
const DEFAULT_DEGRADATION_THRESHOLD = '3';

function getDefaultPort(checkType: CheckType): number | null {
    switch (checkType) {
        case 'HTTP':
            return 80;

        case 'HTTPS':
        case 'SSL':
            return 443;

        case 'DNS':
            return 53;

        case 'PING':
        case 'HEARTBEAT':
            return null;

        case 'TCP':
        default:
            return null;
    }
}

function getDefaultPath(checkType: CheckType): string {
    switch (checkType) {
        case 'HTTP':
        case 'HTTPS':
            return '/';

        default:
            return '';
    }
}

export function CreateServiceDialog({
                                        open,
                                        isSubmitting,
                                        initialNodeId = null,
                                        onClose,
                                        onSubmit,
                                    }: CreateServiceDialogProps) {
    const [nodeId, setNodeId] = useState(initialNodeId?.toString() ?? '');
    const [checkType, setCheckType] = useState<CheckType>(DEFAULT_CHECK_TYPE);
    const [name, setName] = useState('');
    const [targetHost, setTargetHost] = useState('');
    const [port, setPort] = useState(getDefaultPort(DEFAULT_CHECK_TYPE)?.toString() ?? '');
    const [path, setPath] = useState(getDefaultPath(DEFAULT_CHECK_TYPE));
    const [intervalSeconds, setIntervalSeconds] = useState(DEFAULT_INTERVAL_SECONDS);
    const [responseTimeThresholdMs, setResponseTimeThresholdMs] = useState(
        DEFAULT_RESPONSE_TIME_THRESHOLD_MS,
    );
    const [degradationThreshold, setDegradationThreshold] = useState(
        DEFAULT_DEGRADATION_THRESHOLD,
    );

    const isNodeIdLocked = initialNodeId !== null && initialNodeId !== undefined;

    useEffect(() => {
        if (!open) {
            return;
        }

        setNodeId(initialNodeId?.toString() ?? '');
        setCheckType(DEFAULT_CHECK_TYPE);
        setName('');
        setTargetHost('');
        setPort(getDefaultPort(DEFAULT_CHECK_TYPE)?.toString() ?? '');
        setPath(getDefaultPath(DEFAULT_CHECK_TYPE));
        setIntervalSeconds(DEFAULT_INTERVAL_SECONDS);
        setResponseTimeThresholdMs(DEFAULT_RESPONSE_TIME_THRESHOLD_MS);
        setDegradationThreshold(DEFAULT_DEGRADATION_THRESHOLD);
    }, [open, initialNodeId]);

    const handleCheckTypeChange = (value: CheckType) => {
        setCheckType(value);

        const defaultPort = getDefaultPort(value);
        const defaultPath = getDefaultPath(value);

        setPort(defaultPort?.toString() ?? '');
        setPath(defaultPath);
    };

    const handleSubmit = (event: FormSubmitEvent) => {
        event.preventDefault();

        const parsedNodeId = Number(nodeId);
        const parsedPort = port.trim() ? Number(port) : null;
        const parsedIntervalSeconds = Number(intervalSeconds);
        const parsedResponseTimeThresholdMs = Number(responseTimeThresholdMs);
        const parsedDegradationThreshold = Number(degradationThreshold);

        onSubmit({
            nodeId: parsedNodeId,
            checkType,
            name: name.trim(),
            targetHost: targetHost.trim(),
            port: parsedPort,
            path: path.trim() || null,
            intervalSeconds: parsedIntervalSeconds,
            responseTimeThresholdMs: parsedResponseTimeThresholdMs,
            degradationThreshold: parsedDegradationThreshold,
        });
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        onClose();
    };

    const isSubmitDisabled =
        isSubmitting ||
        !nodeId.trim() ||
        Number(nodeId) <= 0 ||
        !name.trim() ||
        !targetHost.trim() ||
        !intervalSeconds.trim() ||
        Number(intervalSeconds) <= 0 ||
        !responseTimeThresholdMs.trim() ||
        Number(responseTimeThresholdMs) <= 0 ||
        !degradationThreshold.trim() ||
        Number(degradationThreshold) <= 0;

    return (
        <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
            <Box component="form" onSubmit={handleSubmit}>
                <DialogTitle>Добавить сервис для мониторинга</DialogTitle>

                <DialogContent>
                    <Stack spacing={2.5} sx={{ mt: 1 }}>
                        <TextField
                            label="ID узла"
                            value={nodeId}
                            onChange={(event) => setNodeId(event.target.value)}
                            helperText={
                                isNodeIdLocked
                                    ? 'ID выбранного узла подставлен автоматически.'
                                    : 'Укажите ID существующего узла.'
                            }
                            required
                            fullWidth
                            disabled={isNodeIdLocked}
                            type="number"
                        />

                        <FormControl fullWidth>
                            <InputLabel>Тип проверки</InputLabel>

                            <Select
                                label="Тип проверки"
                                value={checkType}
                                onChange={(event) =>
                                    handleCheckTypeChange(event.target.value as CheckType)
                                }
                            >
                                <MenuItem value="HTTP">{getCheckTypeLabel('HTTP')}</MenuItem>
                                <MenuItem value="HTTPS">{getCheckTypeLabel('HTTPS')}</MenuItem>
                                <MenuItem value="TCP">{getCheckTypeLabel('TCP')}</MenuItem>
                                <MenuItem value="DNS">{getCheckTypeLabel('DNS')}</MenuItem>
                                <MenuItem value="SSL">{getCheckTypeLabel('SSL')}</MenuItem>
                                <MenuItem value="PING">{getCheckTypeLabel('PING')}</MenuItem>
                                <MenuItem value="HEARTBEAT">
                                    {getCheckTypeLabel('HEARTBEAT')}
                                </MenuItem>
                            </Select>
                        </FormControl>

                        <TextField
                            label="Название сервиса"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                            fullWidth
                        />

                        <TextField
                            label="Проверяемый адрес"
                            value={targetHost}
                            onChange={(event) => setTargetHost(event.target.value)}
                            placeholder="Например: rutube.ru"
                            required
                            fullWidth
                        />

                        <Grid container spacing={2}>
                            <Grid size={6}>
                                <TextField
                                    label="Порт"
                                    value={port}
                                    onChange={(event) => setPort(event.target.value)}
                                    fullWidth
                                    type="number"
                                />
                            </Grid>

                            <Grid size={6}>
                                <TextField
                                    label="Путь"
                                    value={path}
                                    onChange={(event) => setPath(event.target.value)}
                                    placeholder="/"
                                    fullWidth
                                />
                            </Grid>
                        </Grid>

                        <TextField
                            label="Интервал проверки, секунд"
                            value={intervalSeconds}
                            onChange={(event) => setIntervalSeconds(event.target.value)}
                            helperText="Например: 60, 300, 3600"
                            required
                            fullWidth
                            type="number"
                        />

                        <Alert severity="info">
                            Деградация фиксируется, если сервис несколько проверок подряд отвечает медленнее заданного порога.
                        </Alert>

                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Порог медленного ответа, мс"
                                    value={responseTimeThresholdMs}
                                    onChange={(event) =>
                                        setResponseTimeThresholdMs(event.target.value)
                                    }
                                    helperText="Например: 300 для строгого контроля или 1000 для внешних сайтов"
                                    required
                                    fullWidth
                                    type="number"
                                />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Порог деградации"
                                    value={degradationThreshold}
                                    onChange={(event) =>
                                        setDegradationThreshold(event.target.value)
                                    }
                                    helperText="Сколько медленных проверок подряд нужно для подтверждения"
                                    required
                                    fullWidth
                                    type="number"
                                />
                            </Grid>
                        </Grid>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleClose}>
                        Отмена
                    </Button>

                    <Button
                        type="submit"
                        variant="contained"
                        disabled={isSubmitDisabled}
                    >
                        {isSubmitting ? 'Добавление...' : 'Добавить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}