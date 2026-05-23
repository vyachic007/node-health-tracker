import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Container,
    Link,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { useState, type FormEvent } from 'react';
import { authApi } from '../api/authApi';

export function PasswordResetPage() {
    const [email, setEmail] = useState('demo_user@example.com');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        setIsSubmitting(true);
        setError(null);
        setIsSuccess(false);

        try {
            await authApi.requestPasswordReset({ email });
            setIsSuccess(true);
        } catch {
            setError('Не удалось отправить письмо восстановления пароля.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                background:
                    'radial-gradient(circle at top right, rgba(36, 88, 211, 0.18), transparent 35%), radial-gradient(circle at bottom left, rgba(15, 118, 110, 0.16), transparent 32%)',
            }}
        >
            <Container maxWidth="sm">
                <Card
                    elevation={0}
                    sx={{
                        border: 1,
                        borderColor: 'divider',
                        boxShadow: '0 24px 80px rgba(15, 23, 42, 0.16)',
                    }}
                >
                    <CardContent sx={{ p: { xs: 3, sm: 5 } }}>
                        <Stack spacing={3}>
                            <Stack spacing={1}>
                                <Typography variant="h4">Восстановление пароля</Typography>
                                <Typography color="text.secondary">
                                    Укажите email пользователя. Система отправит письмо с token для восстановления.
                                </Typography>
                            </Stack>

                            {isSuccess && (
                                <Alert severity="success">
                                    Запрос отправлен. Проверьте письмо в Mailpit или почтовом ящике.
                                </Alert>
                            )}

                            {error && <Alert severity="error">{error}</Alert>}

                            <Box component="form" onSubmit={handleSubmit}>
                                <Stack spacing={2.5}>
                                    <TextField
                                        label="Email"
                                        type="email"
                                        value={email}
                                        onChange={(event) => setEmail(event.target.value)}
                                        fullWidth
                                        required
                                    />

                                    <Button
                                        type="submit"
                                        variant="contained"
                                        size="large"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? 'Отправка...' : 'Отправить письмо'}
                                    </Button>

                                    <Typography variant="body2">
                                        <Link href="/login" underline="hover">
                                            Вернуться ко входу
                                        </Link>
                                    </Typography>
                                </Stack>
                            </Box>
                        </Stack>
                    </CardContent>
                </Card>
            </Container>
        </Box>
    );
}