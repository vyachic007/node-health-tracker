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
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { useEffect, useState } from 'react';
import { notificationChannelLabels } from '../model/notificationLabels';
import type {
    NotificationSetting,
    UpdateNotificationSettingRequest,
    VkBindLinkResponse,
} from '../model/notificationTypes';
import { formatDateTime } from '../../../shared/lib/formatters';

interface NotificationSettingCardProps {
    setting: NotificationSetting;
    isSaving: boolean;
    isDeleting: boolean;
    isCreatingTelegramLink: boolean;
    isCreatingVkLink: boolean;
    vkBindLink: VkBindLinkResponse | null;
    onSave: (settingId: number, payload: UpdateNotificationSettingRequest) => void;
    onDelete: (setting: NotificationSetting) => void;
    onConnectTelegram: () => void;
    onConnectVk: (settingId: number) => void;
    onCopyVkCommand: (command: string) => void;
}

const MESSENGER_NOT_CONNECTED_VALUE = 'not_connected';

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
            return 'VK подключается через сообщения сообщества. Peer ID сохраняется автоматически после отправки команды /start.';
        default:
            return '';
    }
}

function isMessengerConnected(destination: string | null | undefined) {
    return Boolean(
        destination &&
        destination.trim().length > 0 &&
        destination !== MESSENGER_NOT_CONNECTED_VALUE,
    );
}

export function NotificationSettingCard({
                                            setting,
                                            isSaving,
                                            isDeleting,
                                            isCreatingTelegramLink,
                                            isCreatingVkLink,
                                            vkBindLink,
                                            onSave,
                                            onDelete,
                                            onConnectTelegram,
                                            onConnectVk,
                                            onCopyVkCommand,
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
    const isVk = setting.channel === 'VK';
    const isMessenger = isTelegram || isVk;
    const messengerConnected = isMessengerConnected(destination);

    useEffect(() => {
        setIsEnabled(setting.isEnabled);
        setDestination(setting.destination);
        setNotifyOnIncidentOpen(setting.notifyOnIncidentOpen);
        setNotifyOnIncidentResolved(setting.notifyOnIncidentResolved);
    }, [setting]);

    const handleSave = () => {
        if (isMessenger && !messengerConnected) {
            return;
        }

        onSave(setting.id, {
            isEnabled: isMessenger ? true : isEnabled,
            destination: isMessenger ? destination : destination.trim(),
            notifyOnIncidentOpen,
            notifyOnIncidentResolved,
        });
    };

    const isSaveDisabled =
        isSaving ||
        isDeleting ||
        (!isMessenger && !destination.trim()) ||
        (isMessenger && !messengerConnected);

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

                    {!isMessenger && (
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
                        <Stack spacing={1.5}>
                            <Alert severity={messengerConnected ? 'success' : 'info'}>
                                {messengerConnected
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
                                    : messengerConnected
                                        ? 'Переподключить Telegram'
                                        : 'Подключить Telegram'}
                            </Button>

                            <Typography variant="body2" color="text.secondary">
                                Backend сам получит chat ID и сохранит его в настройках.
                            </Typography>
                        </Stack>
                    )}

                    {isVk && (
                        <Stack spacing={1.5}>
                            <Alert severity={messengerConnected ? 'success' : 'info'}>
                                {messengerConnected
                                    ? `VK подключён. Получатель: ${destination}`
                                    : 'VK ещё не подключён. Нажмите кнопку ниже, затем отправьте команду в сообщения сообщества.'}
                            </Alert>

                            <Button
                                variant="outlined"
                                startIcon={<LinkIcon />}
                                onClick={() => onConnectVk(setting.id)}
                                disabled={isCreatingVkLink || isDeleting}
                                fullWidth
                            >
                                {isCreatingVkLink
                                    ? 'Создание команды...'
                                    : messengerConnected
                                        ? 'Переподключить VK'
                                        : 'Подключить VK'}
                            </Button>

                            {vkBindLink && (
                                <Alert severity="success">
                                    <Stack spacing={1.5}>
                                        <Typography sx={{ fontWeight: 800 }}>
                                            Команда для подключения VK создана
                                        </Typography>

                                        <Typography>
                                            Откройте сообщения сообщества Node Health Tracker и отправьте туда команду:
                                        </Typography>

                                        <Typography
                                            component="code"
                                            sx={{
                                                display: 'block',
                                                p: 1.5,
                                                borderRadius: 2,
                                                bgcolor: 'background.paper',
                                                border: 1,
                                                borderColor: 'divider',
                                                wordBreak: 'break-all',
                                                fontFamily: 'monospace',
                                            }}
                                        >
                                            {vkBindLink.command}
                                        </Typography>

                                        <Stack
                                            direction={{ xs: 'column', sm: 'row' }}
                                            spacing={1}
                                            sx={{ alignItems: { xs: 'stretch', sm: 'center' } }}
                                        >
                                            <Button
                                                variant="contained"
                                                href={vkBindLink.vkLink}
                                                target="_blank"
                                                rel="noreferrer"
                                            >
                                                Открыть VK
                                            </Button>

                                            <Button
                                                variant="outlined"
                                                startIcon={<ContentCopyIcon />}
                                                onClick={() => onCopyVkCommand(vkBindLink.command)}
                                            >
                                                Скопировать команду
                                            </Button>
                                        </Stack>

                                        <Typography variant="body2" color="text.secondary">
                                            Команда действует до {formatDateTime(vkBindLink.expiresAt)}.
                                        </Typography>
                                    </Stack>
                                </Alert>
                            )}

                            <Typography variant="body2" color="text.secondary">
                                Backend сам получит VK peer ID и сохранит его в настройках.
                            </Typography>
                        </Stack>
                    )}

                    {!isMessenger && (
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
                        <Tooltip
                            title={
                                isMessenger && !messengerConnected
                                    ? 'Сначала подключите мессенджер'
                                    : isSaving
                                        ? 'Сохранение...'
                                        : 'Сохранить настройки событий'
                            }
                        >
                            <span>
                                <IconButton
                                    color="primary"
                                    onClick={handleSave}
                                    disabled={isSaveDisabled}
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