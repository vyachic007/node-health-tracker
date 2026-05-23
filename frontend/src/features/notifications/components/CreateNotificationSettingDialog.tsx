import {
    Alert,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    Switch,
    TextField,
    Typography,
} from '@mui/material';
import { useState } from 'react';
import { notificationChannelLabels } from '../model/notificationLabels';
import type {
    CreateNotificationSettingRequest,
    NotificationChannel,
} from '../model/notificationTypes';

interface CreateNotificationSettingDialogProps {
    open: boolean;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (payload: CreateNotificationSettingRequest) => void;
}

function getDestinationLabel(channel: NotificationChannel) {
    switch (channel) {
        case 'EMAIL':
            return 'Email получателя';
        case 'VK':
            return 'VK user ID / peer ID';
        case 'TELEGRAM':
            return 'Telegram получатель';
        default:
            return 'Получатель';
    }
}

function getDestinationPlaceholder(channel: NotificationChannel) {
    switch (channel) {
        case 'EMAIL':
            return 'user@example.com';
        case 'VK':
            return '587701632';
        case 'TELEGRAM':
            return '';
        default:
            return '';
    }
}

export function CreateNotificationSettingDialog({
                                                    open,
                                                    isSubmitting,
                                                    onClose,
                                                    onSubmit,
                                                }: CreateNotificationSettingDialogProps) {
    const [channel, setChannel] = useState<NotificationChannel>('EMAIL');
    const [isEnabled, setIsEnabled] = useState(true);
    const [destination, setDestination] = useState('');
    const [notifyOnIncidentOpen, setNotifyOnIncidentOpen] = useState(true);
    const [notifyOnIncidentResolved, setNotifyOnIncidentResolved] = useState(true);

    const isTelegram = channel === 'TELEGRAM';

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        onClose();
    };

    const handleSubmit = () => {
        onSubmit({
            channel,
            isEnabled: isTelegram ? false : isEnabled,
            destination: isTelegram ? 'not_connected' : destination.trim(),
            notifyOnIncidentOpen,
            notifyOnIncidentResolved,
        });
    };

    const handleChannelChange = (nextChannel: NotificationChannel) => {
        setChannel(nextChannel);

        if (nextChannel === 'TELEGRAM') {
            setDestination('');
            setIsEnabled(false);
            return;
        }

        setIsEnabled(true);
    };

    const isSubmitDisabled =
        isSubmitting || (!isTelegram && destination.trim().length === 0);

    return (
        <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
            <DialogTitle>Добавить канал уведомлений</DialogTitle>

            <DialogContent>
                <Stack spacing={2.25} sx={{ pt: 1 }}>
                    <FormControl fullWidth>
                        <InputLabel>Канал</InputLabel>

                        <Select
                            label="Канал"
                            value={channel}
                            onChange={(event) =>
                                handleChannelChange(event.target.value as NotificationChannel)
                            }
                        >
                            <MenuItem value="EMAIL">
                                {notificationChannelLabels.EMAIL}
                            </MenuItem>

                            <MenuItem value="TELEGRAM">
                                {notificationChannelLabels.TELEGRAM}
                            </MenuItem>

                            <MenuItem value="VK">
                                {notificationChannelLabels.VK}
                            </MenuItem>
                        </Select>
                    </FormControl>

                    {isTelegram ? (
                        <Alert severity="info">
                            Для Telegram не нужно вручную вводить chat ID. После добавления канала появится карточка Telegram. В ней нажмите “Подключить Telegram”, откройте бота и нажмите Start.
                        </Alert>
                    ) : (
                        <TextField
                            label={getDestinationLabel(channel)}
                            value={destination}
                            onChange={(event) => setDestination(event.target.value)}
                            placeholder={getDestinationPlaceholder(channel)}
                            required
                            fullWidth
                        />
                    )}

                    {!isTelegram && (
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isEnabled}
                                    onChange={(event) => setIsEnabled(event.target.checked)}
                                />
                            }
                            label={isEnabled ? 'Канал включён' : 'Канал отключён'}
                        />
                    )}

                    {isTelegram && (
                        <Typography variant="body2" color="text.secondary">
                            Канал будет создан как неподключённый. Он включится автоматически после привязки Telegram через бота.
                        </Typography>
                    )}

                    <Stack>
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={notifyOnIncidentOpen}
                                    onChange={(event) =>
                                        setNotifyOnIncidentOpen(event.target.checked)
                                    }
                                />
                            }
                            label="Уведомлять при открытии инцидента"
                        />

                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={notifyOnIncidentResolved}
                                    onChange={(event) =>
                                        setNotifyOnIncidentResolved(event.target.checked)
                                    }
                                />
                            }
                            label="Уведомлять при закрытии инцидента"
                        />
                    </Stack>
                </Stack>
            </DialogContent>

            <DialogActions sx={{ px: 3, pb: 2 }}>
                <Button onClick={handleClose} disabled={isSubmitting}>
                    Отмена
                </Button>

                <Button
                    variant="contained"
                    onClick={handleSubmit}
                    disabled={isSubmitDisabled}
                >
                    {isSubmitting ? 'Добавление...' : 'Добавить'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}