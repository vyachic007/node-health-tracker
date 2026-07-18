import {
    Alert,
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    Grid,
    IconButton,
    Stack,
    Switch,
    TextField,
    Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useEffect, useState } from 'react';
import { getCheckTypeLabel } from '../model/serviceLabels';
import type { NetworkService, UpdateNetworkServiceRequest } from '../model/serviceTypes';

const SERVICE_TYPE_FIELDS: Record<string, {
    address: boolean;
    port: boolean;
    path: boolean;
}> = {
    HTTP: { address: true, port: false, path: true },
    HTTPS: { address: true, port: false, path: true },
    TCP: { address: true, port: true, path: false },
    DNS: { address: true, port: false, path: false },
    SSL: { address: true, port: true, path: false },
    HEARTBEAT: { address: false, port: false, path: false },
};

const getServiceTypeFields = (type?: string) => {
    const normalizedType = (type || "").toUpperCase();
    return SERVICE_TYPE_FIELDS[normalizedType] ?? { address: true, port: true, path: true };
};

import {
    checkTypeNeedsPath,
    checkTypeNeedsPort,
    checkTypeSupportsDegradation,
    getDefaultPortByCheckType,
} from '../model/serviceTypes';

interface EditServiceDialogProps {
    open: boolean;
    service: NetworkService | null;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (serviceId: number, payload: UpdateNetworkServiceRequest) => void;
}

const DEFAULT_RESPONSE_TIME_THRESHOLD_MS = 1000;
const DEFAULT_DEGRADATION_THRESHOLD = 3;

export function EditServiceDialog({
                                      open,
                                      service,
                                      isSubmitting,
                                      onClose,
                                      onSubmit,
                                  }: EditServiceDialogProps) {
const [name, setName] = useState('');
    const [targetHost, setTargetHost] = useState('');
    const [port, setPort] = useState('');
    const [path, setPath] = useState('');
    const [intervalSeconds, setIntervalSeconds] = useState('');
    const [responseTimeThresholdMs, setResponseTimeThresholdMs] = useState('');
    const [degradationThreshold, setDegradationThreshold] = useState('');
    const [isEnabled, setIsEnabled] = useState(true);
    const [notifyEmail, setNotifyEmail] = useState(true);
    const [notifyTelegram, setNotifyTelegram] = useState(true);
    const [notifyVk, setNotifyVk] = useState(true);

    const needsPort = service ? checkTypeNeedsPort(service.checkType) : false;
    const needsPath = service ? checkTypeNeedsPath(service.checkType) : false;
    const supportsDegradation = service
        ? checkTypeSupportsDegradation(service.checkType)
        : false;

    useEffect(() => {
        if (!service) {
            return;
        }

        const defaultPort = getDefaultPortByCheckType(service.checkType);

        setName(service.name);
        setTargetHost(service.targetHost);
        setPort(
            service.port === null
                ? defaultPort?.toString() ?? ''
                : String(service.port),
        );
        setPath(service.path ?? '');
        setIntervalSeconds(String(service.intervalSeconds));
        setResponseTimeThresholdMs(
            String(service.responseTimeThresholdMs ?? DEFAULT_RESPONSE_TIME_THRESHOLD_MS),
        );
        setDegradationThreshold(
            String(service.degradationThreshold ?? DEFAULT_DEGRADATION_THRESHOLD),
        );
        setIsEnabled(service.isEnabled);
        setNotifyEmail(service.notifyEmail);
        setNotifyTelegram(service.notifyTelegram);
        setNotifyVk(service.notifyVk);
    }, [service]);

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        onClose();
    };

    const handleSubmit = () => {
        if (!service) {
            return;
        }

        onSubmit(service.id, {
            checkType: service.checkType,
            name: name.trim(),
            targetHost: targetHost.trim(),
            port: needsPort && port.trim() ? Number(port) : null,
            path: needsPath ? path.trim() || null : null,
            intervalSeconds: Number(intervalSeconds),
            isEnabled,
            responseTimeThresholdMs: supportsDegradation
                ? Number(responseTimeThresholdMs)
                : DEFAULT_RESPONSE_TIME_THRESHOLD_MS,
            degradationThreshold: supportsDegradation
                ? Number(degradationThreshold)
                : DEFAULT_DEGRADATION_THRESHOLD,
            notifyEmail,
            notifyTelegram,
            notifyVk,
        });
    };

    const isFormInvalid =
        !name.trim() ||
        !targetHost.trim() ||
        !intervalSeconds.trim() ||
        Number(intervalSeconds) <= 0 ||
        (needsPort && (!port.trim() || Number(port) <= 0)) ||
        (supportsDegradation && (
            !responseTimeThresholdMs.trim() ||
            Number(responseTimeThresholdMs) <= 0 ||
            !degradationThreshold.trim() ||
            Number(degradationThreshold) <= 0
        ));

    const degradationAlertSeverity: 'warning' | 'success' =
        service?.degraded ? 'warning' : 'success';

        const visibleFields = getServiceTypeFields(service?.checkType);

return (
        <Dialog
            open={open}
            onClose={handleClose}
            fullWidth
            maxWidth="sm"
        >
            <Box>
                <DialogTitle
                    sx={{
                        pr: 7,
                        position: 'relative',
                    }}
                >
                    Редактировать сервис

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
                    <Stack spacing={2.5} sx={{ mt: 1 }}>
                        {service && (
                            <Box>
                                <Typography color="text.secondary">
                                    Тип проверки
                                </Typography>

                                <Typography sx={{ fontWeight: 800 }}>
                                    {getCheckTypeLabel(service.checkType)}
                                </Typography>

                                <Alert severity="info" sx={{ mt: 1.5 }}>
                                    Тип проверки при редактировании не меняется. Остальные параметры можно обновить.
                                </Alert>
                            </Box>
                        )}

                        <TextField
                            label="Название сервиса"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                            fullWidth
                        />

                        {visibleFields.address && (
<TextField
                            label="Адрес / домен"
                            value={targetHost}
                            onChange={(event) => setTargetHost(event.target.value)}
                            required
                            fullWidth
                        />
)}

                        {(needsPort || needsPath) && (
                            <Grid container spacing={2}>
                                {needsPort && (
                                    <Grid size={{ xs: 12, sm: needsPath ? 6 : 12 }}>
                                        {visibleFields.port && (
<TextField
                                            label="Порт"
                                            type="number"
                                            value={port}
                                            onChange={(event) => setPort(event.target.value)}
                                            placeholder="80, 443, 5432..."
                                            required
                                            fullWidth
                                        />
)}
                                    </Grid>
                                )}

                                {needsPath && (
                                    <Grid size={{ xs: 12, sm: needsPort ? 6 : 12 }}>
                                        {visibleFields.path && (
<TextField
                                            label="Путь HTTP-запроса"
                                            value={path}
                                            onChange={(event) => setPath(event.target.value)}
                                            placeholder="/ или /api/health"
                                            fullWidth
                                        />
)}
                                    </Grid>
                                )}
                            </Grid>
                        )}

                        <TextField
                            label={
                                service?.checkType === 'HEARTBEAT'
                                    ? 'Интервал ожидания сигнала, секунд'
                                    : 'Интервал проверки, секунд'
                            }
                            type="number"
                            value={intervalSeconds}
                            onChange={(event) => setIntervalSeconds(event.target.value)}
                            helperText={
                                service?.checkType === 'HEARTBEAT'
                                    ? 'Если сигнал не придёт за несколько интервалов, heartbeat будет считаться устаревшим'
                                    : 'Например: 60 — раз в минуту, 300 — раз в 5 минут, 3600 — раз в час'
                            }
                            required
                            fullWidth
                        />

                        {supportsDegradation && (
                            <>
                                <Alert severity="info">
                                    Деградация используется для фиксации ситуации, когда сервис формально доступен, но отвечает слишком медленно.
                                </Alert>

                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <TextField
                                            label="Порог медленного ответа, мс"
                                            type="number"
                                            value={responseTimeThresholdMs}
                                            onChange={(event) =>
                                                setResponseTimeThresholdMs(event.target.value)
                                            }
                                            helperText="Если ответ дольше этого значения, проверка считается медленной"
                                            required
                                            fullWidth
                                        />
                                    </Grid>

                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <TextField
                                            label="Порог деградации"
                                            type="number"
                                            value={degradationThreshold}
                                            onChange={(event) =>
                                                setDegradationThreshold(event.target.value)
                                            }
                                            helperText="Сколько медленных проверок подряд нужно для подтверждения деградации"
                                            required
                                            fullWidth
                                        />
                                    </Grid>
                                </Grid>

                                {service && (
                                    <Alert severity={degradationAlertSeverity}>
                                        Сейчас медленных проверок подряд: {service.consecutiveDegradations}. Для подтверждения деградации нужно: {service.degradationThreshold}.
                                    </Alert>
                                )}
                            </>
                        )}

                        <Box>
                            <Typography variant="subtitle2" sx={{ mb: 1 }}>
                                Уведомления по этому сервису
                            </Typography>

                            <Stack spacing={0.5}>
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={notifyEmail}
                                            onChange={(event) =>
                                                setNotifyEmail(event.target.checked)
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления на Email"
                                />

                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={notifyTelegram}
                                            onChange={(event) =>
                                                setNotifyTelegram(event.target.checked)
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления в Telegram"
                                />

                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={notifyVk}
                                            onChange={(event) =>
                                                setNotifyVk(event.target.checked)
                                            }
                                        />
                                    }
                                    label="Отправлять уведомления во VK"
                                />
                            </Stack>
                        </Box>

                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isEnabled}
                                    onChange={(event) => setIsEnabled(event.target.checked)}
                                />
                            }
                            label={isEnabled ? 'Сервис включён' : 'Сервис отключён'}
                        />
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleClose} disabled={isSubmitting}>
                        Отмена
                    </Button>

                    <Button
                        type="button"
                        variant="contained"
                        onClick={handleSubmit}
                        disabled={isSubmitting || isFormInvalid}
                    >
                        {isSubmitting ? 'Сохранение...' : 'Сохранить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}