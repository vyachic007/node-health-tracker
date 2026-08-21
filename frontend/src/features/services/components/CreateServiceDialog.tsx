import {
    Alert,
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    Grid,
    IconButton,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useEffect, useState } from 'react';

import { getCheckTypeLabel } from '../model/serviceLabels';

import type {
    CheckType,
    CreateNetworkServiceRequest,
} from '../model/serviceTypes';

import {
    checkTypeNeedsPath,
    checkTypeNeedsPort,
    checkTypeSupportsDegradation,
    getDefaultPathByCheckType,
    getDefaultPortByCheckType,
} from '../model/serviceTypes';

const SERVICE_TYPE_FIELDS: Record<
    string,
    {
        address: boolean;
        port: boolean;
        path: boolean;
    }
> = {
    HTTP: {
        address: true,
        port: false,
        path: true,
    },
    HTTPS: {
        address: true,
        port: false,
        path: true,
    },
    TCP: {
        address: true,
        port: true,
        path: false,
    },
    DNS: {
        address: true,
        port: false,
        path: false,
    },
    SSL: {
        address: true,
        port: true,
        path: false,
    },
    HEARTBEAT: {
        address: false,
        port: false,
        path: false,
    },
};

const getServiceTypeFields = (type?: string) => {
    const normalizedType = (type || '').toUpperCase();

    return (
        SERVICE_TYPE_FIELDS[normalizedType] ?? {
            address: true,
            port: true,
            path: true,
        }
    );
};

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

export function CreateServiceDialog({
                                        open,
                                        isSubmitting,
                                        initialNodeId = null,
                                        onClose,
                                        onSubmit,
                                    }: CreateServiceDialogProps) {
    const [nodeId, setNodeId] = useState(
        initialNodeId?.toString() ?? '',
    );

    const [checkType, setCheckType] =
        useState<CheckType>(DEFAULT_CHECK_TYPE);

    const [name, setName] = useState('');

    const [targetHost, setTargetHost] = useState('');

    const [port, setPort] = useState(
        getDefaultPortByCheckType(DEFAULT_CHECK_TYPE)?.toString() ?? '',
    );

    const [path, setPath] = useState(
        getDefaultPathByCheckType(DEFAULT_CHECK_TYPE),
    );

    const [intervalSeconds, setIntervalSeconds] = useState(
        DEFAULT_INTERVAL_SECONDS,
    );

    const [responseTimeThresholdMs, setResponseTimeThresholdMs] =
        useState(DEFAULT_RESPONSE_TIME_THRESHOLD_MS);

    const [degradationThreshold, setDegradationThreshold] =
        useState(DEFAULT_DEGRADATION_THRESHOLD);

    const [notifyEmail, setNotifyEmail] = useState(true);
    const [notifyTelegram, setNotifyTelegram] = useState(true);
    const [notifyVk, setNotifyVk] = useState(true);

    const isNodeIdLocked =
        initialNodeId !== null &&
        initialNodeId !== undefined;

    const needsPort = checkTypeNeedsPort(checkType);
    const needsPath = checkTypeNeedsPath(checkType);
    const supportsDegradation =
        checkTypeSupportsDegradation(checkType);

    const visibleFields =
        getServiceTypeFields(checkType);

    useEffect(() => {
        if (!open) {
            return;
        }

        setNodeId(
            initialNodeId?.toString() ?? '',
        );

        setCheckType(
            DEFAULT_CHECK_TYPE,
        );

        setName('');

        setTargetHost('');

        setPort(
            getDefaultPortByCheckType(
                DEFAULT_CHECK_TYPE,
            )?.toString() ?? '',
        );

        setPath(
            getDefaultPathByCheckType(
                DEFAULT_CHECK_TYPE,
            ),
        );

        setIntervalSeconds(
            DEFAULT_INTERVAL_SECONDS,
        );

        setResponseTimeThresholdMs(
            DEFAULT_RESPONSE_TIME_THRESHOLD_MS,
        );

        setDegradationThreshold(
            DEFAULT_DEGRADATION_THRESHOLD,
        );

        setNotifyEmail(true);
        setNotifyTelegram(true);
        setNotifyVk(true);
    }, [open, initialNodeId]);

    const handleCheckTypeChange = (
        value: CheckType,
    ) => {
        setCheckType(value);

        const defaultPort =
            getDefaultPortByCheckType(value);

        const defaultPath =
            getDefaultPathByCheckType(value);

        setPort(
            defaultPort?.toString() ?? '',
        );

        setPath(defaultPath);

        setResponseTimeThresholdMs(
            DEFAULT_RESPONSE_TIME_THRESHOLD_MS,
        );

        setDegradationThreshold(
            DEFAULT_DEGRADATION_THRESHOLD,
        );

        if (
            !getServiceTypeFields(value).address
        ) {
            setTargetHost('');
        }
    };

    const handleSubmit = (
        event: FormSubmitEvent,
    ) => {
        event.preventDefault();

        const parsedNodeId =
            Number(nodeId);

        const parsedPort =
            port.trim()
                ? Number(port)
                : null;

        const parsedIntervalSeconds =
            Number(intervalSeconds);

        const parsedResponseTimeThresholdMs =
            Number(responseTimeThresholdMs);

        const parsedDegradationThreshold =
            Number(degradationThreshold);

        onSubmit({
            nodeId: parsedNodeId,
            checkType,
            name: name.trim(),

            targetHost:
                visibleFields.address
                    ? targetHost.trim()
                    : '',

            port:
                needsPort
                    ? parsedPort
                    : null,

            path:
                needsPath
                    ? path.trim() || null
                    : null,

            intervalSeconds:
            parsedIntervalSeconds,

            responseTimeThresholdMs:
                supportsDegradation
                    ? parsedResponseTimeThresholdMs
                    : Number(
                        DEFAULT_RESPONSE_TIME_THRESHOLD_MS,
                    ),

            degradationThreshold:
                supportsDegradation
                    ? parsedDegradationThreshold
                    : Number(
                        DEFAULT_DEGRADATION_THRESHOLD,
                    ),

            notifyEmail,
            notifyTelegram,
            notifyVk,
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

        (
            visibleFields.address &&
            !targetHost.trim()
        ) ||

        !intervalSeconds.trim() ||
        Number(intervalSeconds) <= 0 ||

        (
            needsPort &&
            (
                !port.trim() ||
                Number(port) <= 0
            )
        ) ||

        (
            supportsDegradation &&
            (
                !responseTimeThresholdMs.trim() ||
                Number(
                    responseTimeThresholdMs,
                ) <= 0 ||

                !degradationThreshold.trim() ||
                Number(
                    degradationThreshold,
                ) <= 0
            )
        );

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            fullWidth
            maxWidth="sm"
        >
            <Box
                component="form"
                onSubmit={handleSubmit}
            >
                <DialogTitle
                    sx={{
                        pr: 7,
                        position: 'relative',
                    }}
                >
                    Добавить сервис для мониторинга

                    <IconButton
                        aria-label="Закрыть окно"
                        onClick={handleClose}
                        disabled={isSubmitting}
                        sx={{
                            position: 'absolute',
                            right: 12,
                            top: 12,
                        }}
                    >
                        <CloseIcon />
                    </IconButton>
                </DialogTitle>

                <DialogContent>
                    <Stack
                        spacing={2.5}
                        sx={{ mt: 1 }}
                    >
                        <TextField
                            label="ID узла"
                            value={nodeId}
                            onChange={(event) =>
                                setNodeId(
                                    event.target.value,
                                )
                            }
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
                            <InputLabel>
                                Тип проверки
                            </InputLabel>

                            <Select
                                label="Тип проверки"
                                value={checkType}
                                onChange={(event) =>
                                    handleCheckTypeChange(
                                        event.target.value as CheckType,
                                    )
                                }
                            >
                                <MenuItem value="HTTP">
                                    {getCheckTypeLabel(
                                        'HTTP',
                                    )}
                                </MenuItem>

                                <MenuItem value="HTTPS">
                                    {getCheckTypeLabel(
                                        'HTTPS',
                                    )}
                                </MenuItem>

                                <MenuItem value="TCP">
                                    {getCheckTypeLabel(
                                        'TCP',
                                    )}
                                </MenuItem>

                                <MenuItem value="DNS">
                                    {getCheckTypeLabel(
                                        'DNS',
                                    )}
                                </MenuItem>

                                <MenuItem value="SSL">
                                    {getCheckTypeLabel(
                                        'SSL',
                                    )}
                                </MenuItem>

                                <MenuItem value="PING">
                                    {getCheckTypeLabel(
                                        'PING',
                                    )}
                                </MenuItem>

                                <MenuItem value="HEARTBEAT">
                                    {getCheckTypeLabel(
                                        'HEARTBEAT',
                                    )}
                                </MenuItem>
                            </Select>
                        </FormControl>

                        <TextField
                            label="Название сервиса"
                            value={name}
                            onChange={(event) =>
                                setName(
                                    event.target.value,
                                )
                            }
                            required
                            fullWidth
                        />

                        {visibleFields.address && (
                            <TextField
                                label="Адрес / домен"
                                value={targetHost}
                                onChange={(event) =>
                                    setTargetHost(
                                        event.target.value,
                                    )
                                }
                                placeholder="Например: rutube.ru"
                                required
                                fullWidth
                            />
                        )}

                        {(needsPort || needsPath) && (
                            <Grid
                                container
                                spacing={2}
                            >
                                {needsPort && (
                                    <Grid
                                        size={
                                            needsPath
                                                ? 6
                                                : 12
                                        }
                                    >
                                        {visibleFields.port && (
                                            <TextField
                                                label="Порт"
                                                value={port}
                                                onChange={(event) =>
                                                    setPort(
                                                        event.target.value,
                                                    )
                                                }
                                                required
                                                fullWidth
                                                type="number"
                                            />
                                        )}
                                    </Grid>
                                )}

                                {needsPath && (
                                    <Grid
                                        size={
                                            needsPort
                                                ? 6
                                                : 12
                                        }
                                    >
                                        {visibleFields.path && (
                                            <TextField
                                                label="Путь HTTP-запроса"
                                                value={path}
                                                onChange={(event) =>
                                                    setPath(
                                                        event.target.value,
                                                    )
                                                }
                                                placeholder="/"
                                                fullWidth
                                            />
                                        )}
                                    </Grid>
                                )}
                            </Grid>
                        )}

                        <TextField
                            label={
                                checkType ===
                                'HEARTBEAT'
                                    ? 'Интервал ожидания сигнала, секунд'
                                    : 'Интервал проверки, секунд'
                            }
                            value={intervalSeconds}
                            onChange={(event) =>
                                setIntervalSeconds(
                                    event.target.value,
                                )
                            }
                            helperText={
                                checkType ===
                                'HEARTBEAT'
                                    ? 'Если сигнал не придёт за несколько интервалов, heartbeat будет считаться устаревшим'
                                    : 'Например: 60, 300, 3600'
                            }
                            required
                            fullWidth
                            type="number"
                        />

                        {supportsDegradation && (
                            <>
                                <Alert severity="info">
                                    Деградация фиксируется,
                                    если сервис несколько
                                    проверок подряд отвечает
                                    медленнее заданного порога.
                                </Alert>

                                <Grid
                                    container
                                    spacing={2}
                                >
                                    <Grid
                                        size={{
                                            xs: 12,
                                            sm: 6,
                                        }}
                                    >
                                        <TextField
                                            label="Порог медленного ответа, мс"
                                            value={
                                                responseTimeThresholdMs
                                            }
                                            onChange={(event) =>
                                                setResponseTimeThresholdMs(
                                                    event
                                                        .target
                                                        .value,
                                                )
                                            }
                                            helperText="Например: 300 для строгого контроля или 1000 для внешних сайтов"
                                            required
                                            fullWidth
                                            type="number"
                                        />
                                    </Grid>

                                    <Grid
                                        size={{
                                            xs: 12,
                                            sm: 6,
                                        }}
                                    >
                                        <TextField
                                            label="Порог деградации"
                                            value={
                                                degradationThreshold
                                            }
                                            onChange={(event) =>
                                                setDegradationThreshold(
                                                    event
                                                        .target
                                                        .value,
                                                )
                                            }
                                            helperText="Сколько медленных проверок подряд нужно для подтверждения"
                                            required
                                            fullWidth
                                            type="number"
                                        />
                                    </Grid>
                                </Grid>
                            </>
                        )}

                        <Box>
                            <Typography
                                variant="subtitle2"
                                sx={{ mb: 1 }}
                            >
                                Уведомления по этому сервису
                            </Typography>

                            <Stack spacing={0.5}>
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={
                                                notifyEmail
                                            }
                                            onChange={(event) =>
                                                setNotifyEmail(
                                                    event
                                                        .target
                                                        .checked,
                                                )
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления на Email"
                                />

                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={
                                                notifyTelegram
                                            }
                                            onChange={(event) =>
                                                setNotifyTelegram(
                                                    event
                                                        .target
                                                        .checked,
                                                )
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления в Telegram"
                                />

                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={
                                                notifyVk
                                            }
                                            onChange={(event) =>
                                                setNotifyVk(
                                                    event
                                                        .target
                                                        .checked,
                                                )
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления во VK"
                                />
                            </Stack>
                        </Box>
                    </Stack>
                </DialogContent>

                <DialogActions
                    sx={{
                        px: 3,
                        pb: 3,
                    }}
                >
                    <Button
                        onClick={handleClose}
                        disabled={isSubmitting}
                    >
                        Отмена
                    </Button>

                    <Button
                        type="submit"
                        variant="contained"
                        disabled={isSubmitDisabled}
                    >
                        {isSubmitting
                            ? 'Добавление...'
                            : 'Добавить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}