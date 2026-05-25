import {
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    FormControl,
    MenuItem,
    Select,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Tooltip,
    Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import type { AdminUser, AdminUserRole } from '../model/adminUserTypes';
import {
    adminUserRoleLabels,
    adminUserStatusLabels,
    getUserStatusColor,
} from '../model/adminUserLabels';
import { formatDateTime } from '../../../shared/lib/formatters';

interface AdminUsersTableProps {
    users: AdminUser[];
    currentUserId: number | null;
    changingUserId: number | null;
    onBlock: (user: AdminUser) => void;
    onUnblock: (user: AdminUser) => void;
    onChangeRole: (user: AdminUser, role: AdminUserRole) => void;
}

export function AdminUsersTable({
                                    users,
                                    currentUserId,
                                    changingUserId,
                                    onBlock,
                                    onUnblock,
                                    onChangeRole,
                                }: AdminUsersTableProps) {
    return (
        <Card
            elevation={0}
            sx={{
                border: 1,
                borderColor: 'divider',
            }}
        >
            <CardContent>
                <Stack spacing={2}>
                    <Box>
                        <Typography variant="h5">
                            Список пользователей
                        </Typography>

                        <Typography color="text.secondary">
                            Управление ролями и доступом пользователей к системе.
                        </Typography>
                    </Box>

                    <Box sx={{ overflowX: 'auto' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>ID</TableCell>
                                    <TableCell>Пользователь</TableCell>
                                    <TableCell>Email</TableCell>
                                    <TableCell>Роль</TableCell>
                                    <TableCell>Статус</TableCell>
                                    <TableCell>Создан</TableCell>
                                    <TableCell>Обновлён</TableCell>
                                    <TableCell align="right">Действия</TableCell>
                                </TableRow>
                            </TableHead>

                            <TableBody>
                                {users.map((user) => {
                                    const isCurrentUser = currentUserId === user.id;
                                    const isChanging = changingUserId === user.id;

                                    return (
                                        <TableRow key={user.id} hover>
                                            <TableCell>
                                                №{user.id}
                                            </TableCell>

                                            <TableCell>
                                                <Stack spacing={0.5}>
                                                    <Typography sx={{ fontWeight: 800 }}>
                                                        {user.username}
                                                    </Typography>

                                                    {isCurrentUser && (
                                                        <Typography variant="caption" color="text.secondary">
                                                            Текущий аккаунт
                                                        </Typography>
                                                    )}
                                                </Stack>
                                            </TableCell>

                                            <TableCell>
                                                {user.email}
                                            </TableCell>

                                            <TableCell>
                                                <FormControl size="small" sx={{ minWidth: 170 }}>
                                                    <Select
                                                        value={user.role}
                                                        disabled={isChanging || isCurrentUser}
                                                        onChange={(event) =>
                                                            onChangeRole(
                                                                user,
                                                                event.target.value as AdminUserRole,
                                                            )
                                                        }
                                                    >
                                                        <MenuItem value="ROLE_USER">
                                                            {adminUserRoleLabels.ROLE_USER}
                                                        </MenuItem>

                                                        <MenuItem value="ROLE_ADMIN">
                                                            {adminUserRoleLabels.ROLE_ADMIN}
                                                        </MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </TableCell>

                                            <TableCell>
                                                <Chip
                                                    label={adminUserStatusLabels[user.status]}
                                                    color={getUserStatusColor(user.status)}
                                                    size="small"
                                                />
                                            </TableCell>

                                            <TableCell>
                                                {formatDateTime(user.createdAt)}
                                            </TableCell>

                                            <TableCell>
                                                {formatDateTime(user.updatedAt)}
                                            </TableCell>

                                            <TableCell align="right">
                                                {user.status === 'ACTIVE' ? (
                                                    <Tooltip
                                                        title={
                                                            isCurrentUser
                                                                ? 'Нельзя заблокировать собственный аккаунт'
                                                                : 'Заблокировать пользователя'
                                                        }
                                                    >
                                                        <span>
                                                            <Button
                                                                variant="outlined"
                                                                color="error"
                                                                startIcon={<BlockIcon />}
                                                                disabled={isChanging || isCurrentUser}
                                                                onClick={() => onBlock(user)}
                                                            >
                                                                Заблокировать
                                                            </Button>
                                                        </span>
                                                    </Tooltip>
                                                ) : (
                                                    <Button
                                                        variant="outlined"
                                                        color="success"
                                                        startIcon={<LockOpenIcon />}
                                                        disabled={isChanging}
                                                        onClick={() => onUnblock(user)}
                                                    >
                                                        Разблокировать
                                                    </Button>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}

                                {users.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={8}>
                                            <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                                                Пользователи не найдены.
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </Box>
                </Stack>
            </CardContent>
        </Card>
    );
}