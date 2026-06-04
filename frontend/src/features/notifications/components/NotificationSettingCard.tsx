import {
    Alert,
    Avatar,
    Box,
    Button,
    Card,
    CardContent,
    Checkbox,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    IconButton,
    Menu,
    MenuItem,
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
import MoreVertIcon from '@mui/icons-material/MoreVert';
import SettingsIcon from '@mui/icons-material/Settings';
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
            return <EmailIcon fontSize="small" />;

        case 'TELEGRAM':
            return <TelegramIcon fontSize="small" />;

        case 'VK':
            return <ChatIcon fontSize="small" />;

        default:
            return <ChatIcon fontSize="small" />;
    }
}

function getChannelAvatarColor(channel: NotificationSetting['channel']) {
    switch (channel) {
        case 'EMAIL':
            return '#2563eb';

        case 'TELEGRAM':
            return '#229ed9';

        case 'VK':
            return '#0077ff';

        default:
            return '#2563eb';
    }
}

function getDestinationTitle(channel: NotificationSetting['channel']) {
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

function getDestinationShortLabel(setting: NotificationSetting, destination: string) {
    const safeDestination = destination || MESSENGER_NOT_CONNECTED_VALUE;

    if (setting.channel === 'EMAIL') {
        return safeDestination;
    }

    if (setting.channel === 'TELEGRAM') {
        return isMessengerConnected(safeDestination)
            ? `Chat ID: ${safeDestination}`
            : 'Telegram не подключён';
    }

    if (setting.channel === 'VK') {
        return isMessengerConnected(safeDestination)
            ? `Peer ID: ${safeDestination}`
            : 'VK не подключён';
    }

    return safeDestination;
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

function getEventsLabel(
    notifyOnIncidentOpen: boolean,
    notifyOnIncidentResolved: boolean,
) {
    if (notifyOnIncidentOpen && notifyOnIncidentResolved) {
        return 'Открытие, закрытие';
    }

    if (notifyOnIncidentOpen) {
        return 'Только открытие';
    }

    if (notifyOnIncidentResolved) {
        return 'Только закрытие';
    }

    return 'События отключены';
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
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);
    const [menuAnchorEl, setMenuAnchorEl] = useState<HTMLElement | null>(null);

    const isTelegram = setting.channel === 'TELEGRAM';
    const isVk = setting.channel === 'VK';
    const isMessenger = isTelegram || isVk;
    const messengerConnected = isMessengerConnected(destination);
    const isMenuOpen = Boolean(menuAnchorEl);

    useEffect(() => {
        setIsEnabled(setting.isEnabled);
        setDestination(setting.destination);
        setNotifyOnIncidentOpen(setting.notifyOnIncidentOpen);
        setNotifyOnIncidentResolved(setting.notifyOnIncidentResolved);
    }, [setting]);

    const buildSavePayload = (
        nextIsEnabled = isEnabled,
        nextDestination = destination,
        nextNotifyOnIncidentOpen = notifyOnIncidentOpen,
        nextNotifyOnIncidentResolved = notifyOnIncidentResolved,
    ): UpdateNotificationSettingRequest => ({
        isEnabled: nextIsEnabled,
        destination: isMessenger ? nextDestination : nextDestination.trim(),
        notifyOnIncidentOpen: nextNotifyOnIncidentOpen,
        notifyOnIncidentResolved: nextNotifyOnIncidentResolved,
    });

    const handleEnabledChange = (nextIsEnabled: boolean) => {
        if (isMessenger && !messengerConnected) {
            return;
        }

        setIsEnabled(nextIsEnabled);

        onSave(setting.id, buildSavePayload(nextIsEnabled));
    };

    const handleSave = () => {
        if (isMessenger && !messengerConnected) {
            return;
        }

        onSave(setting.id, buildSavePayload());

        setIsSettingsOpen(false);
    };

    const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setMenuAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setMenuAnchorEl(null);
    };

    const handleDelete = () => {
        handleMenuClose();
        onDelete(setting);
    };

    const handleConnect = () => {
        handleMenuClose();

        if (isTelegram) {
            onConnectTelegram();
            return;
        }

        if (isVk) {
            onConnectVk(setting.id);
        }
    };

    const isSaveDisabled =
        isSaving ||
        isDeleting ||
        (!isMessenger && !destination.trim()) ||
        (isMessenger && !messengerConnected);

    const destinationLabel = getDestinationShortLabel(setting, destination);
    const eventsLabel = getEventsLabel(
        notifyOnIncidentOpen,
        notifyOnIncidentResolved,
    );

    return (
        <>
            <Card
                elevation={0}
                sx={{
                    height: '100%',
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1.5,
                }}
            >
                <CardContent sx={{ p: 2.25 }}>
                    <Stack spacing={2}>
                        <Stack
                            direction="row"
                            spacing={1.5}
                            sx={{
                                alignItems: 'center',
                                justifyContent: 'space-between',
                            }}
                        >
                            <Stack
                                direction="row"
                                spacing={1.5}
                                sx={{ alignItems: 'center', minWidth: 0 }}
                            >
                                <Avatar
                                    sx={{
                                        width: 36,
                                        height: 36,
                                        bgcolor: getChannelAvatarColor(setting.channel),
                                    }}
                                >
                                    {getChannelIcon(setting.channel)}
                                </Avatar>

                                <Box sx={{ minWidth: 0 }}>
                                    <Typography
                                        variant="h6"
                                        sx={{ fontWeight: 900, lineHeight: 1.1 }}
                                        noWrap
                                    >
                                        {notificationChannelLabels[setting.channel]}
                                    </Typography>

                                    <Typography
                                        variant="body2"
                                        color="text.secondary"
                                        noWrap
                                    >
                                        {destinationLabel}
                                    </Typography>
                                </Box>
                            </Stack>

                            <Switch
                                checked={isEnabled}
                                onChange={(event) => handleEnabledChange(event.target.checked)}
                                disabled={
                                    isSaving ||
                                    isDeleting ||
                                    (isMessenger && !messengerConnected)
                                }
                            />
                        </Stack>

                        <Stack spacing={0.75}>
                            <Typography variant="body2" color="text.secondary">
                                События: {eventsLabel}
                            </Typography>

                            {!isEnabled && (
                                <Chip
                                    label="Канал отключён"
                                    size="small"
                                    color="default"
                                    sx={{ width: 'fit-content' }}
                                />
                            )}

                            {isMessenger && !messengerConnected && (
                                <Alert severity="info" sx={{ py: 0.5 }}>
                                    Канал нужно подключить перед использованием.
                                </Alert>
                            )}
                        </Stack>

                        <Stack
                            direction="row"
                            spacing={1}
                            sx={{
                                justifyContent: 'space-between',
                                alignItems: 'center',
                            }}
                        >
                            <Button
                                variant="outlined"
                                size="small"
                                startIcon={<SettingsIcon />}
                                onClick={() => setIsSettingsOpen(true)}
                            >
                                Настроить
                            </Button>

                            <Tooltip title="Дополнительные действия">
                                <span>
                                    <IconButton
                                        size="small"
                                        onClick={handleMenuOpen}
                                        disabled={isSaving || isDeleting}
                                    >
                                        <MoreVertIcon />
                                    </IconButton>
                                </span>
                            </Tooltip>
                        </Stack>
                    </Stack>
                </CardContent>
            </Card>

            <Menu
                anchorEl={menuAnchorEl}
                open={isMenuOpen}
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
                {isMessenger && (
                    <MenuItem
                        onClick={handleConnect}
                        disabled={
                            isTelegram
                                ? isCreatingTelegramLink
                                : isCreatingVkLink
                        }
                    >
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            <LinkIcon fontSize="small" />
                            <span>
                                {messengerConnected ? 'Переподключить' : 'Подключить'}
                            </span>
                        </Stack>
                    </MenuItem>
                )}

                <MenuItem onClick={handleDelete} sx={{ color: 'error.main' }}>
                    <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                        <DeleteIcon fontSize="small" />
                        <span>Удалить</span>
                    </Stack>
                </MenuItem>
            </Menu>

            <Dialog
                open={isSettingsOpen}
                onClose={() => setIsSettingsOpen(false)}
                fullWidth
                maxWidth="sm"
            >
                <DialogTitle>
                    Настройка канала: {notificationChannelLabels[setting.channel]}
                </DialogTitle>

                <DialogContent>
                    <Stack spacing={2.25} sx={{ mt: 1 }}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isEnabled}
                                    onChange={(event) => setIsEnabled(event.target.checked)}
                                    disabled={isMessenger && !messengerConnected}
                                />
                            }
                            label={isEnabled ? 'Канал включён' : 'Канал отключён'}
                        />

                        {!isMessenger && (
                            <TextField
                                label={getDestinationTitle(setting.channel)}
                                value={destination}
                                onChange={(event) => setDestination(event.target.value)}
                                helperText={getDestinationHelperText(setting.channel)}
                                fullWidth
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
                                                    borderRadius: 1.5,
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
                                                sx={{
                                                    alignItems: {
                                                        xs: 'stretch',
                                                        sm: 'center',
                                                    },
                                                }}
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
                                                    onClick={() =>
                                                        onCopyVkCommand(vkBindLink.command)
                                                    }
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

                        <Box>
                            <Typography sx={{ fontWeight: 800, mb: 1 }}>
                                События отправки
                            </Typography>

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
                        </Box>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2.5 }}>
                    <Button onClick={() => setIsSettingsOpen(false)}>
                        Отмена
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<SaveIcon />}
                        onClick={handleSave}
                        disabled={isSaveDisabled}
                    >
                        {isSaving ? 'Сохранение...' : 'Сохранить'}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}
