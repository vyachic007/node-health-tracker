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
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { authApi } from '../api/authApi';
import { useAuth } from '../store/AuthContext';

export function RegisterPage() {
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const { login } = useAuth();

    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [repeatPassword, setRepeatPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setError(null);

        const trimmedUsername = username.trim();
        const trimmedEmail = email.trim();

        if (password !== repeatPassword) {
            setError('Пароли не совпадают.');
            return;
        }

        setIsSubmitting(true);

        try {
            await authApi.register({
                username: trimmedUsername,
                email: trimmedEmail,
                password,
            });

            await login({
                username: trimmedUsername,
                password,
            });

            enqueueSnackbar('Регистрация выполнена успешно', {
                variant: 'success',
            });

            navigate('/dashboard', { replace: true });
        } catch {
            setError('Не удалось зарегистрироваться. Проверьте данные или попробуйте другой логин/email.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const isSubmitDisabled =
        isSubmitting ||
        username.trim().length === 0 ||
        email.trim().length === 0 ||
        password.trim().length < 8 ||
        repeatPassword.trim().length < 8;

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                background:
                    'radial-gradient(circle at top left, rgba(36, 88, 211, 0.20), transparent 34%), radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.18), transparent 28%)',
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
                            <Stack spacing={1} sx={{ alignItems: 'center' }}>
                                <Box
                                    sx={{
                                        width: 64,
                                        height: 64,
                                        borderRadius: 4,
                                        display: 'grid',
                                        placeItems: 'center',
                                        color: 'primary.main',
                                        bgcolor: 'primary.main',
                                        background:
                                            'linear-gradient(135deg, rgba(36, 88, 211, 0.16), rgba(94, 234, 212, 0.18))',
                                    }}
                                >
                                    <MonitorHeartIcon fontSize="large" />
                                </Box>

                                <Typography variant="h4" sx={{ textAlign: 'center' }}>
                                    Регистрация
                                </Typography>

                                <Typography color="text.secondary" sx={{ textAlign: 'center' }}>
                                    Создайте аккаунт для работы с мониторингом сервисов
                                </Typography>
                            </Stack>

                            {error && <Alert severity="error">{error}</Alert>}

                            <Stack spacing={2.5}>
                                <TextField
                                    label="Логин"
                                    value={username}
                                    onChange={(event) => setUsername(event.target.value)}
                                    helperText="Будет использоваться для входа в систему"
                                    fullWidth
                                    required
                                />

                                <TextField
                                    label="Email"
                                    type="email"
                                    value={email}
                                    onChange={(event) => setEmail(event.target.value)}
                                    fullWidth
                                    required
                                />

                                <TextField
                                    label="Пароль"
                                    type="password"
                                    value={password}
                                    onChange={(event) => setPassword(event.target.value)}
                                    helperText="Минимум 8 символов"
                                    fullWidth
                                    required
                                />

                                <TextField
                                    label="Повторите пароль"
                                    type="password"
                                    value={repeatPassword}
                                    onChange={(event) => setRepeatPassword(event.target.value)}
                                    fullWidth
                                    required
                                />

                                <Button
                                    type="button"
                                    variant="contained"
                                    size="large"
                                    onClick={handleSubmit}
                                    disabled={isSubmitDisabled}
                                >
                                    {isSubmitting ? 'Создание аккаунта...' : 'Зарегистрироваться'}
                                </Button>

                                <Typography variant="body2" sx={{ textAlign: 'center' }}>
                                    Уже есть аккаунт?{' '}
                                    <Link
                                        component={RouterLink}
                                        to="/login"
                                        underline="hover"
                                        sx={{ fontWeight: 700 }}
                                    >
                                        Войти
                                    </Link>
                                </Typography>
                            </Stack>
                        </Stack>
                    </CardContent>
                </Card>
            </Container>
        </Box>
    );
}