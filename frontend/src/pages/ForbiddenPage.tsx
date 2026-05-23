import { Button, Card, CardContent, Stack, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

export function ForbiddenPage() {
    return (
        <Stack alignItems="center" justifyContent="center" sx={{ minHeight: '70vh' }}>
            <Card elevation={0} sx={{ maxWidth: 520, border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Stack spacing={2}>
                        <Typography variant="h4">Доступ запрещён</Typography>
                        <Typography color="text.secondary">
                            У вашей роли нет прав для просмотра этой страницы.
                        </Typography>
                        <Button component={Link} to="/dashboard" variant="contained">
                            Вернуться на dashboard
                        </Button>
                    </Stack>
                </CardContent>
            </Card>
        </Stack>
    );
}