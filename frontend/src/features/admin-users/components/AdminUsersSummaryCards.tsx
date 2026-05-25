import {
    Card,
    CardContent,
    Grid,
    Stack,
    Typography,
} from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import PersonIcon from '@mui/icons-material/Person';
import BlockIcon from '@mui/icons-material/Block';
import type { AdminUser } from '../model/adminUserTypes';

interface AdminUsersSummaryCardsProps {
    users: AdminUser[];
}

export function AdminUsersSummaryCards({ users }: AdminUsersSummaryCardsProps) {
    const total = users.length;
    const admins = users.filter((user) => user.role === 'ROLE_ADMIN').length;
    const regularUsers = users.filter((user) => user.role === 'ROLE_USER').length;
    const blocked = users.filter((user) => user.status === 'BLOCKED').length;

    const cards = [
        {
            title: 'Всего пользователей',
            value: total,
            icon: <PeopleIcon color="primary" />,
        },
        {
            title: 'Администраторы',
            value: admins,
            icon: <AdminPanelSettingsIcon color="primary" />,
        },
        {
            title: 'Обычные пользователи',
            value: regularUsers,
            icon: <PersonIcon color="primary" />,
        },
        {
            title: 'Заблокированные',
            value: blocked,
            icon: <BlockIcon color="error" />,
        },
    ];

    return (
        <Grid container spacing={2}>
            {cards.map((card) => (
                <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                    <Card
                        elevation={0}
                        sx={{
                            height: '100%',
                            border: 1,
                            borderColor: 'divider',
                        }}
                    >
                        <CardContent>
                            <Stack spacing={2}>
                                <Stack
                                    direction="row"
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                    }}
                                >
                                    <Typography sx={{ fontWeight: 800 }}>
                                        {card.title}
                                    </Typography>

                                    {card.icon}
                                </Stack>

                                <Typography variant="h3">
                                    {card.value}
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}