import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    Grid,
    Stack,
    Switch,
    TextField,
    Typography,
} from '@mui/material';
import { useEffect, useState } from 'react';
import { getCheckTypeLabel } from '../model/serviceLabels';
import type { NetworkService, UpdateNetworkServiceRequest } from '../model/serviceTypes';

interface EditServiceDialogProps {
    open: boolean;
    service: NetworkService | null;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (serviceId: number, payload: UpdateNetworkServiceRequest) => void;
}

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
    const [isEnabled, setIsEnabled] = useState(true);

    useEffect(() => {
        if (!service) {
            return;
        }

        setName(service.name);
        setTargetHost(service.targetHost);
        setPort(service.port === null ? '' : String(service.port));
        setPath(service.path ?? '');
        setIntervalSeconds(String(service.intervalSeconds));
        setIsEnabled(service.isEnabled);
    }, [service]);

    const handleSubmit = () => {
        if (!service) {
            return;
        }

        onSubmit(service.id, {
            checkType: service.checkType,
            name: name.trim(),
            targetHost: targetHost.trim(),
            port: port.trim() ? Number(port) : null,
            path: path.trim() || null,
            intervalSeconds: Number(intervalSeconds),
            isEnabled,
        });
    };

    const isFormInvalid =
        !name.trim() ||
        !targetHost.trim() ||
        !intervalSeconds.trim() ||
        Number(intervalSeconds) <= 0 ||
        (port.trim().length > 0 && Number(port) <= 0);

    return (
        <Dialog
            open={open}
            onClose={isSubmitting ? undefined : onClose}
            fullWidth
            maxWidth="sm"
        >
            <Box>
                <DialogTitle>Редактировать сервис</DialogTitle>

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

                        <TextField
                            label="Проверяемый адрес"
                            value={targetHost}
                            onChange={(event) => setTargetHost(event.target.value)}
                            required
                            fullWidth
                        />

                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Порт"
                                    type="number"
                                    value={port}
                                    onChange={(event) => setPort(event.target.value)}
                                    placeholder="80, 443, 5432..."
                                    fullWidth
                                />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Путь"
                                    value={path}
                                    onChange={(event) => setPath(event.target.value)}
                                    placeholder="/ или /api/health"
                                    fullWidth
                                />
                            </Grid>
                        </Grid>

                        <TextField
                            label="Интервал проверки, секунд"
                            type="number"
                            value={intervalSeconds}
                            onChange={(event) => setIntervalSeconds(event.target.value)}
                            helperText="Например: 60 — раз в минуту, 300 — раз в 5 минут, 3600 — раз в час"
                            required
                            fullWidth
                        />

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
                    <Button onClick={onClose} disabled={isSubmitting}>
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