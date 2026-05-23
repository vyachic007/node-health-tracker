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
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { useAuth } from '../store/AuthContext';

export function LoginPage() {
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const { login } = useAuth();

    const [username, setUsername] = useState('demo_user');
    const [password, setPassword] = useState('12345678');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            await login({ username, password });
            enqueueSnackbar('Вход выполнен успешно', { variant: 'success' });
            navigate('/dashboard', { replace: true });
        } catch {
            setError('Не удалось войти. Проверьте логин и пароль.');
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
                            <Stack spacing={1} alignItems="center">
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

                                <Typography variant="h4" textAlign="center">
                                    Node Health Tracker
                                </Typography>

                                <Typography color="text.secondary" textAlign="center">
                                    Диагностический мониторинг сервисов, инцидентов и уведомлений
                                </Typography>
                            </Stack>

                            {error && <Alert severity="error">{error}</Alert>}

                            <Box component="form" onSubmit={handleSubmit}>
                                <Stack spacing={2.5}>
                                    <TextField
                                        label="Логин"
                                        value={username}
                                        onChange={(event) => setUsername(event.target.value)}
                                        fullWidth
                                        required
                                    />

                                    <TextField
                                        label="Пароль"
                                        type="password"
                                        value={password}
                                        onChange={(event) => setPassword(event.target.value)}
                                        fullWidth
                                        required
                                    />

                                    <Button
                                        type="submit"
                                        variant="contained"
                                        size="large"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? 'Выполняется вход...' : 'Войти'}
                                    </Button>

                                    <Typography variant="body2" textAlign="center">
                                        <Link href="/password-reset" underline="hover">
                                            Забыли пароль?
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