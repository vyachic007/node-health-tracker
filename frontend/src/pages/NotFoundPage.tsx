import { Button, Card, CardContent, Stack, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

export function NotFoundPage() {
    return (
        <Stack sx={{ minHeight: '70vh', alignItems: 'center', justifyContent: 'center' }}>
            <Card elevation={0} sx={{ maxWidth: 520, border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Stack spacing={2}>
                        <Typography variant="h4">Страница не найдена</Typography>
                        <Typography color="text.secondary">
                            Возможно, ссылка устарела или страница ещё не реализована.
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