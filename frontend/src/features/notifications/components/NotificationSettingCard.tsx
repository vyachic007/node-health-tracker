import {
    Alert,
    Button,
    Card,
    CardContent,
    Checkbox,
    Chip,
    FormControlLabel,
    IconButton,
    Stack,
    Switch,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import SaveIcon from '@mui/icons-material/Save';
import EmailIcon from '@mui/icons-material/Email';
import TelegramIcon from '@mui/icons-material/Telegram';
import ChatIcon from '@mui/icons-material/Chat';
import LinkIcon from '@mui/icons-material/Link';
import { useEffect, useState } from 'react';
import { notificationChannelLabels } from '../model/notificationLabels';
import type {
    NotificationSetting,
    UpdateNotificationSettingRequest,
} from '../model/notificationTypes';

interface NotificationSettingCardProps {
    setting: NotificationSetting;
    isSaving: boolean;
    isDeleting: boolean;
    isCreatingTelegramLink: boolean;
    onSave: (settingId: number, payload: UpdateNotificationSettingRequest) => void;
    onDelete: (setting: NotificationSetting) => void;
    onConnectTelegram: () => void;
}

const TELEGRAM_NOT_CONNECTED_VALUE = 'not_connected';

function getChannelIcon(channel: NotificationSetting['channel']) {
    switch (channel) {
        case 'EMAIL':
            return <EmailIcon />;
        case 'TELEGRAM':
            return <TelegramIcon />;
        case 'VK':
            return <ChatIcon />;
        default:
            return <ChatIcon />;
    }
}

function getDestinationLabel(channel: NotificationSetting['channel']) {
    switch (channel) {
        case 'EMAIL':
            return 'Email получателя';
        case 'TELEGRAM':
            return 'Telegram получатель';
        case 'VK':
            return 'VK получатель';
        default:
            return 'Получатель';
    }
}

function getDestinationHelperText(channel: NotificationSetting['channel']) {
    switch (channel) {
        case 'EMAIL':
            return 'На этот адрес будут приходить уведомления об инцидентах.';
        case 'TELEGRAM':
            return 'Telegram подключается через бота. Chat ID сохраняется автоматически после нажатия Start.';
        case 'VK':
            return 'Укажите VK user ID или peer ID беседы, куда нужно отправлять уведомления.';
        default:
            return '';
    }
}

function isTelegramConnected(destination: string | null | undefined) {
    return Boolean(
        destination &&
        destination.trim().length > 0 &&
        destination !== TELEGRAM_NOT_CONNECTED_VALUE,
    );
}

export function NotificationSettingCard({
                                            setting,
                                            isSaving,
                                            isDeleting,
                                            isCreatingTelegramLink,
                                            onSave,
                                            onDelete,
                                            onConnectTelegram,
                                        }: NotificationSettingCardProps) {
    const [isEnabled, setIsEnabled] = useState(setting.isEnabled);
    const [destination, setDestination] = useState(setting.destination);
    const [notifyOnIncidentOpen, setNotifyOnIncidentOpen] = useState(
        setting.notifyOnIncidentOpen,
    );
    const [notifyOnIncidentResolved, setNotifyOnIncidentResolved] = useState(
        setting.notifyOnIncidentResolved,
    );

    const isTelegram = setting.channel === 'TELEGRAM';
    const telegramConnected = isTelegramConnected(destination);

    useEffect(() => {
        setIsEnabled(setting.isEnabled);
        setDestination(setting.destination);
        setNotifyOnIncidentOpen(setting.notifyOnIncidentOpen);
        setNotifyOnIncidentResolved(setting.notifyOnIncidentResolved);
    }, [setting]);

    const handleSave = () => {
        onSave(setting.id, {
            isEnabled: isTelegram ? telegramConnected : isEnabled,
            destination: isTelegram
                ? destination
                : destination.trim(),
            notifyOnIncidentOpen,
            notifyOnIncidentResolved,
        });
    };

    return (
        <Card
            elevation={0}
            sx={{
                height: '100%',
                border: 1,
                borderColor: isEnabled ? 'primary.main' : 'divider',
            }}
        >
            <CardContent>
                <Stack spacing={2.25}>
                    <Stack
                        direction="row"
                        spacing={1.5}
                        sx={{ justifyContent: 'space-between', alignItems: 'center' }}
                    >
                        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                            {getChannelIcon(setting.channel)}

                            <Typography variant="h6">
                                {notificationChannelLabels[setting.channel]}
                            </Typography>
                        </Stack>

                        <Chip
                            label={isEnabled ? 'Включено' : 'Отключено'}
                            color={isEnabled ? 'success' : 'default'}
                            size="small"
                        />
                    </Stack>

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

                    {isTelegram ? (
                        <Stack spacing={1.5}>
                            <Alert severity={telegramConnected ? 'success' : 'info'}>
                                {telegramConnected
                                    ? `Telegram подключён. Получатель: ${destination}`
                                    : 'Telegram ещё не подключён. Нажмите кнопку ниже, откройте бота и нажмите Start.'}
                            </Alert>

                            <Button
                                variant="outlined"
                                startIcon={<LinkIcon />}
                                onClick={onConnectTelegram}
                                disabled={isCreatingTelegramLink || isDeleting}
                                fullWidth
                            >
                                {isCreatingTelegramLink
                                    ? 'Создание ссылки...'
                                    : telegramConnected
                                        ? 'Переподключить Telegram'
                                        : 'Подключить Telegram'}
                            </Button>

                            <Typography variant="body2" color="text.secondary">
                                После открытия Telegram нажмите Start. Backend сам получит chat ID и сохранит его в настройках.
                            </Typography>
                        </Stack>
                    ) : (
                        <TextField
                            label={getDestinationLabel(setting.channel)}
                            value={destination}
                            onChange={(event) => setDestination(event.target.value)}
                            helperText={getDestinationHelperText(setting.channel)}
                            fullWidth
                        />
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

                    <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                        <Tooltip title={isSaving ? 'Сохранение...' : 'Сохранить настройки событий'}>
                            <span>
                                <IconButton
                                    color="primary"
                                    onClick={handleSave}
                                    disabled={
                                        isSaving ||
                                        isDeleting ||
                                        (!isTelegram && !destination.trim())
                                    }
                                    sx={{
                                        width: 48,
                                        height: 48,
                                        border: 1,
                                        borderColor: 'primary.main',
                                    }}
                                >
                                    <SaveIcon />
                                </IconButton>
                            </span>
                        </Tooltip>

                        <Tooltip title="Удалить настройку">
                            <span>
                                <IconButton
                                    color="error"
                                    onClick={() => onDelete(setting)}
                                    disabled={isSaving || isDeleting}
                                    sx={{
                                        width: 48,
                                        height: 48,
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