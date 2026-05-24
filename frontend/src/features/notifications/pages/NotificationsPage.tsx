import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    LinearProgress,
    Stack,
    Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useState } from 'react';
import { notificationsApi } from '../api/notificationsApi';
import { CreateNotificationSettingDialog } from '../components/CreateNotificationSettingDialog';
import { NotificationSettingCard } from '../components/NotificationSettingCard';
import { NotificationsSummaryCards } from '../components/NotificationsSummaryCards';
import { SentNotificationsTable } from '../components/SentNotificationsTable';
import { notificationChannelLabels } from '../model/notificationLabels';
import type {
    CreateNotificationSettingRequest,
    NotificationSetting,
    UpdateNotificationSettingRequest,
    VkBindLinkResponse,
} from '../model/notificationTypes';

export function NotificationsPage() {
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [settingToDelete, setSettingToDelete] = useState<NotificationSetting | null>(null);
    const [savingSettingId, setSavingSettingId] = useState<number | null>(null);
    const [deletingSettingId, setDeletingSettingId] = useState<number | null>(null);
    const [creatingVkLinkSettingId, setCreatingVkLinkSettingId] = useState<number | null>(null);
    const [vkBindLinksBySettingId, setVkBindLinksBySettingId] = useState<
        Record<number, VkBindLinkResponse>
    >({});

    const {
        data: settings = [],
        isLoading: isSettingsLoading,
        isError: isSettingsError,
    } = useQuery({
        queryKey: ['notifications', 'settings'],
        queryFn: notificationsApi.getSettings,
    });

    const {
        data: sentNotifications = [],
        isLoading: isSentLoading,
        isError: isSentError,
    } = useQuery({
        queryKey: ['notifications', 'sent'],
        queryFn: notificationsApi.getSentNotifications,
    });

    const createSettingMutation = useMutation({
        mutationFn: notificationsApi.createSetting,
        onSuccess: () => {
            enqueueSnackbar('Канал уведомлений добавлен', { variant: 'success' });
            setIsCreateOpen(false);

            queryClient.invalidateQueries({ queryKey: ['notifications', 'settings'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось добавить канал уведомлений', { variant: 'error' });
        },
    });

    const updateSettingMutation = useMutation({
        mutationFn: ({
                         settingId,
                         payload,
                     }: {
            settingId: number;
            payload: UpdateNotificationSettingRequest;
        }) => notificationsApi.updateSetting(settingId, payload),
        onMutate: ({ settingId }) => {
            setSavingSettingId(settingId);
        },
        onSuccess: () => {
            enqueueSnackbar('Настройка уведомлений сохранена', { variant: 'success' });

            queryClient.invalidateQueries({ queryKey: ['notifications', 'settings'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось сохранить настройку', { variant: 'error' });
        },
        onSettled: () => {
            setSavingSettingId(null);
        },
    });

    const deleteSettingMutation = useMutation({
        mutationFn: notificationsApi.deleteSetting,
        onMutate: (settingId: number) => {
            setDeletingSettingId(settingId);
        },
        onSuccess: () => {
            enqueueSnackbar('Настройка уведомлений удалена', { variant: 'success' });
            setSettingToDelete(null);

            queryClient.invalidateQueries({ queryKey: ['notifications', 'settings'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось удалить настройку', { variant: 'error' });
        },
        onSettled: () => {
            setDeletingSettingId(null);
        },
    });

    const createTelegramBindLinkMutation = useMutation({
        mutationFn: notificationsApi.createTelegramBindLink,
    });

    const createVkBindLinkMutation = useMutation({
        mutationFn: notificationsApi.createVkBindLink,
    });

    const startTemporarySettingsRefresh = () => {
        const refreshInterval = window.setInterval(() => {
            queryClient.invalidateQueries({ queryKey: ['notifications', 'settings'] });
        }, 3000);

        window.setTimeout(() => {
            window.clearInterval(refreshInterval);
        }, 60000);
    };

    const handleCreateSetting = (payload: CreateNotificationSettingRequest) => {
        createSettingMutation.mutate(payload);
    };

    const handleUpdateSetting = (
        settingId: number,
        payload: UpdateNotificationSettingRequest,
    ) => {
        updateSettingMutation.mutate({ settingId, payload });
    };

    const handleConfirmDelete = () => {
        if (!settingToDelete) {
            return;
        }

        deleteSettingMutation.mutate(settingToDelete.id);
    };

    const handleConnectTelegram = () => {
        const telegramWindow = window.open('', '_blank');

        if (telegramWindow) {
            telegramWindow.document.write(
                '<p style="font-family: Arial, sans-serif; padding: 24px;">Открываем Telegram...</p>',
            );
        }

        createTelegramBindLinkMutation.mutate(undefined, {
            onSuccess: (response) => {
                if (telegramWindow) {
                    telegramWindow.location.href = response.telegramLink;
                } else {
                    window.location.href = response.telegramLink;
                }

                enqueueSnackbar(
                    'Открываем Telegram. Нажмите Start в боте для завершения подключения.',
                    { variant: 'info' },
                );

                startTemporarySettingsRefresh();
            },
            onError: () => {
                if (telegramWindow) {
                    telegramWindow.close();
                }

                enqueueSnackbar('Не удалось создать ссылку подключения Telegram', {
                    variant: 'error',
                });
            },
        });
    };

    const handleConnectVk = (settingId: number) => {
        setCreatingVkLinkSettingId(settingId);

        createVkBindLinkMutation.mutate(undefined, {
            onSuccess: (response) => {
                setVkBindLinksBySettingId((current) => ({
                    ...current,
                    [settingId]: response,
                }));

                enqueueSnackbar('Команда для подключения VK создана', {
                    variant: 'success',
                });

                startTemporarySettingsRefresh();
            },
            onError: () => {
                enqueueSnackbar('Не удалось создать команду подключения VK', {
                    variant: 'error',
                });
            },
            onSettled: () => {
                setCreatingVkLinkSettingId(null);
            },
        });
    };

    const handleCopyVkCommand = async (command: string) => {
        try {
            await navigator.clipboard.writeText(command);

            enqueueSnackbar('Команда VK скопирована', {
                variant: 'success',
            });
        } catch {
            enqueueSnackbar('Не удалось скопировать команду', {
                variant: 'error',
            });
        }
    };

    const isLoading = isSettingsLoading || isSentLoading;

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isSettingsError) {
        return <Alert severity="error">Не удалось загрузить настройки уведомлений.</Alert>;
    }

    return (
        <Stack spacing={3}>
            <Stack
                direction={{ xs: 'column', md: 'row' }}
                spacing={2}
                sx={{
                    justifyContent: 'space-between',
                    alignItems: { xs: 'stretch', md: 'flex-start' },
                }}
            >
                <Box>
                    <Typography variant="h4">
                        Уведомления
                    </Typography>

                    <Typography color="text.secondary">
                        Управление каналами Email, Telegram и VK, настройка событий отправки и просмотр истории уведомлений.
                    </Typography>
                </Box>

                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setIsCreateOpen(true)}
                >
                    Добавить канал
                </Button>
            </Stack>

            <NotificationsSummaryCards
                settings={settings}
                sentNotifications={sentNotifications}
            />

            <Box>
                <Typography variant="h5" sx={{ mb: 2 }}>
                    Каналы уведомлений
                </Typography>

                <Grid container spacing={2}>
                    {settings.map((setting) => (
                        <Grid key={setting.id} size={{ xs: 12, md: 6, xl: 4 }}>
                            <NotificationSettingCard
                                setting={setting}
                                isSaving={savingSettingId === setting.id}
                                isDeleting={deletingSettingId === setting.id}
                                isCreatingTelegramLink={
                                    setting.channel === 'TELEGRAM' &&
                                    createTelegramBindLinkMutation.isPending
                                }
                                isCreatingVkLink={
                                    setting.channel === 'VK' &&
                                    creatingVkLinkSettingId === setting.id
                                }
                                vkBindLink={vkBindLinksBySettingId[setting.id] ?? null}
                                onSave={handleUpdateSetting}
                                onDelete={(selectedSetting) => setSettingToDelete(selectedSetting)}
                                onConnectTelegram={handleConnectTelegram}
                                onConnectVk={handleConnectVk}
                                onCopyVkCommand={handleCopyVkCommand}
                            />
                        </Grid>
                    ))}
                </Grid>

                {settings.length === 0 && (
                    <Alert severity="info">
                        Каналы уведомлений пока не настроены. Нажмите “Добавить канал”.
                    </Alert>
                )}
            </Box>

            {isSentError ? (
                <Alert severity="warning">
                    Не удалось загрузить историю отправленных уведомлений.
                </Alert>
            ) : (
                <SentNotificationsTable notifications={sentNotifications} />
            )}

            <CreateNotificationSettingDialog
                open={isCreateOpen}
                isSubmitting={createSettingMutation.isPending}
                onClose={() => setIsCreateOpen(false)}
                onSubmit={handleCreateSetting}
            />

            <Dialog
                open={Boolean(settingToDelete)}
                onClose={() => setSettingToDelete(null)}
                fullWidth
                maxWidth="xs"
            >
                <DialogTitle>Удалить канал уведомлений?</DialogTitle>

                <DialogContent>
                    <Stack spacing={1}>
                        <Typography>
                            Настройка канала будет удалена.
                        </Typography>

                        <Typography sx={{ fontWeight: 800 }}>
                            {settingToDelete
                                ? notificationChannelLabels[settingToDelete.channel]
                                : ''}
                        </Typography>

                        <Alert severity="warning">
                            После удаления система перестанет отправлять уведомления через этот канал.
                        </Alert>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2 }}>
                    <Button onClick={() => setSettingToDelete(null)}>
                        Отмена
                    </Button>

                    <Button
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={handleConfirmDelete}
                        disabled={deleteSettingMutation.isPending}
                    >
                        {deleteSettingMutation.isPending ? 'Удаление...' : 'Удалить'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Stack>
    );
}