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
    LinearProgress,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useMemo, useState } from 'react';
import { adminUsersApi } from '../api/adminUsersApi';
import { AdminUsersSummaryCards } from '../components/AdminUsersSummaryCards';
import { AdminUsersTable } from '../components/AdminUsersTable';
import type { AdminUser, AdminUserRole, AdminUserStatus } from '../model/adminUserTypes';
import { adminUserRoleLabels, adminUserStatusLabels } from '../model/adminUserLabels';

type UserRoleFilter = 'ALL' | AdminUserRole;
type UserStatusFilter = 'ALL' | AdminUserStatus;

function filterUsers(
    users: AdminUser[],
    search: string,
    roleFilter: UserRoleFilter,
    statusFilter: UserStatusFilter,
) {
    const normalizedSearch = search.trim().toLowerCase();

    return users.filter((user) => {
        const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
        const matchesStatus = statusFilter === 'ALL' || user.status === statusFilter;

        const searchableText = [
            user.id.toString(),
            user.username,
            user.email,
            user.role,
            user.status,
        ]
            .join(' ')
            .toLowerCase();

        const matchesSearch =
            !normalizedSearch || searchableText.includes(normalizedSearch);

        return matchesRole && matchesStatus && matchesSearch;
    });
}

export function AdminUsersPage() {
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();

    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRoleFilter>('ALL');
    const [statusFilter, setStatusFilter] = useState<UserStatusFilter>('ALL');
    const [changingUserId, setChangingUserId] = useState<number | null>(null);
    const [userToBlock, setUserToBlock] = useState<AdminUser | null>(null);

    const {
        data: users = [],
        isLoading,
        isError,
        isFetching,
        refetch,
    } = useQuery({
        queryKey: ['admin', 'users'],
        queryFn: adminUsersApi.getUsers,
    });

    const blockUserMutation = useMutation({
        mutationFn: adminUsersApi.blockUser,
        onMutate: (userId) => {
            setChangingUserId(userId);
        },
        onSuccess: () => {
            enqueueSnackbar('Пользователь заблокирован', { variant: 'success' });
            setUserToBlock(null);
            queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось заблокировать пользователя', { variant: 'error' });
        },
        onSettled: () => {
            setChangingUserId(null);
        },
    });

    const unblockUserMutation = useMutation({
        mutationFn: adminUsersApi.unblockUser,
        onMutate: (userId) => {
            setChangingUserId(userId);
        },
        onSuccess: () => {
            enqueueSnackbar('Пользователь разблокирован', { variant: 'success' });
            queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось разблокировать пользователя', { variant: 'error' });
        },
        onSettled: () => {
            setChangingUserId(null);
        },
    });

    const updateRoleMutation = useMutation({
        mutationFn: ({
                         userId,
                         role,
                     }: {
            userId: number;
            role: AdminUserRole;
        }) => adminUsersApi.updateUserRole(userId, role),
        onMutate: ({ userId }) => {
            setChangingUserId(userId);
        },
        onSuccess: () => {
            enqueueSnackbar('Роль пользователя изменена', { variant: 'success' });
            queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось изменить роль пользователя', { variant: 'error' });
        },
        onSettled: () => {
            setChangingUserId(null);
        },
    });

    const sortedUsers = useMemo(() => {
        return [...users].sort((a, b) => a.id - b.id);
    }, [users]);

    const visibleUsers = useMemo(() => {
        return filterUsers(sortedUsers, search, roleFilter, statusFilter);
    }, [sortedUsers, search, roleFilter, statusFilter]);

    const handleBlock = (selectedUser: AdminUser) => {
        setUserToBlock(selectedUser);
    };

    const handleConfirmBlock = () => {
        if (!userToBlock) {
            return;
        }

        blockUserMutation.mutate(userToBlock.id);
    };

    const handleUnblock = (selectedUser: AdminUser) => {
        unblockUserMutation.mutate(selectedUser.id);
    };

    const handleChangeRole = (selectedUser: AdminUser, role: AdminUserRole) => {
        if (selectedUser.role === role) {
            return;
        }

        updateRoleMutation.mutate({
            userId: selectedUser.id,
            role,
        });
    };

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError) {
        return (
            <Alert severity="error">
                Не удалось загрузить список пользователей. Проверьте backend endpoint /api/admin/users.
            </Alert>
        );
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
                        Пользователи
                    </Typography>

                    <Typography color="text.secondary">
                        Администрирование пользователей, статусов и ролей.
                    </Typography>
                </Box>

                <Button
                    variant="outlined"
                    startIcon={<RefreshIcon />}
                    onClick={() => refetch()}
                    disabled={isFetching}
                >
                    {isFetching ? 'Обновление...' : 'Обновить'}
                </Button>
            </Stack>

            <AdminUsersSummaryCards users={users} />

            <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                        label="Поиск"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                        placeholder="Логин, email, ID, роль или статус"
                        fullWidth
                    />
                </Grid>

                <Grid size={{ xs: 12, md: 3 }}>
                    <FormControl fullWidth>
                        <InputLabel>Роль</InputLabel>

                        <Select
                            label="Роль"
                            value={roleFilter}
                            onChange={(event) =>
                                setRoleFilter(event.target.value as UserRoleFilter)
                            }
                        >
                            <MenuItem value="ALL">Все роли</MenuItem>

                            <MenuItem value="ROLE_USER">
                                {adminUserRoleLabels.ROLE_USER}
                            </MenuItem>

                            <MenuItem value="ROLE_ADMIN">
                                {adminUserRoleLabels.ROLE_ADMIN}
                            </MenuItem>
                        </Select>
                    </FormControl>
                </Grid>

                <Grid size={{ xs: 12, md: 3 }}>
                    <FormControl fullWidth>
                        <InputLabel>Статус</InputLabel>

                        <Select
                            label="Статус"
                            value={statusFilter}
                            onChange={(event) =>
                                setStatusFilter(event.target.value as UserStatusFilter)
                            }
                        >
                            <MenuItem value="ALL">Все статусы</MenuItem>

                            <MenuItem value="ACTIVE">
                                {adminUserStatusLabels.ACTIVE}
                            </MenuItem>

                            <MenuItem value="BLOCKED">
                                {adminUserStatusLabels.BLOCKED}
                            </MenuItem>
                        </Select>
                    </FormControl>
                </Grid>
            </Grid>

            <Typography color="text.secondary">
                Показано пользователей: {visibleUsers.length} из {users.length}
            </Typography>

            <AdminUsersTable
                users={visibleUsers}
                currentUserId={null}
                changingUserId={changingUserId}
                onBlock={handleBlock}
                onUnblock={handleUnblock}
                onChangeRole={handleChangeRole}
            />

            <Dialog
                open={Boolean(userToBlock)}
                onClose={() => setUserToBlock(null)}
                fullWidth
                maxWidth="xs"
            >
                <DialogTitle>Заблокировать пользователя?</DialogTitle>

                <DialogContent>
                    <Stack spacing={1.5}>
                        <Typography>
                            Пользователь потеряет доступ к системе и не сможет войти в аккаунт.
                        </Typography>

                        <Typography sx={{ fontWeight: 800 }}>
                            {userToBlock?.username}
                        </Typography>

                        <Alert severity="warning">
                            После блокировки пользователь не сможет авторизоваться в системе.
                        </Alert>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2 }}>
                    <Button onClick={() => setUserToBlock(null)}>
                        Отмена
                    </Button>

                    <Button
                        variant="contained"
                        color="error"
                        onClick={handleConfirmBlock}
                        disabled={blockUserMutation.isPending}
                    >
                        {blockUserMutation.isPending ? 'Блокировка...' : 'Заблокировать'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Stack>
    );
}