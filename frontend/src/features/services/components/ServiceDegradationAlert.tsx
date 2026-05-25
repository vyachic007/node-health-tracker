import {
    Alert,
    Box,
    Chip,
    LinearProgress,
    Stack,
    Typography,
} from '@mui/material';
import SpeedIcon from '@mui/icons-material/Speed';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

interface ServiceDegradationAlertProps {
    lastStatus: 'UP' | 'DOWN' | null;
    lastResponseTimeMs: number | null;
    responseTimeThresholdMs: number | null;
    degradationThreshold: number | null;
    consecutiveDegradations: number | null;
    degraded: boolean | null;
}

function calculateProgress(
    consecutiveDegradations: number | null,
    degradationThreshold: number | null,
) {
    if (!consecutiveDegradations || !degradationThreshold || degradationThreshold <= 0) {
        return 0;
    }

    return Math.min((consecutiveDegradations / degradationThreshold) * 100, 100);
}

export function ServiceDegradationAlert({
                                            lastStatus,
                                            lastResponseTimeMs,
                                            responseTimeThresholdMs,
                                            degradationThreshold,
                                            consecutiveDegradations,
                                            degraded,
                                        }: ServiceDegradationAlertProps) {
    const hasThreshold = responseTimeThresholdMs !== null && responseTimeThresholdMs > 0;

    if (!hasThreshold) {
        return null;
    }

    const safeConsecutiveDegradations = consecutiveDegradations ?? 0;
    const safeDegradationThreshold = degradationThreshold ?? 0;

    const isSlow =
        lastStatus === 'UP' &&
        lastResponseTimeMs !== null &&
        lastResponseTimeMs > responseTimeThresholdMs;

    const progress = calculateProgress(
        safeConsecutiveDegradations,
        safeDegradationThreshold,
    );

    if (!degraded && !isSlow && safeConsecutiveDegradations === 0) {
        return (
            <Alert severity="success" icon={<SpeedIcon />}>
                <Stack spacing={0.75}>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                        Деградация не обнаружена
                    </Typography>

                    <Typography variant="body2">
                        Последнее время ответа находится в пределах нормы.
                        Порог: {responseTimeThresholdMs} мс.
                    </Typography>
                </Stack>
            </Alert>
        );
    }

    return (
        <Alert
            severity={degraded ? 'warning' : 'info'}
            icon={degraded ? <WarningAmberIcon /> : <SpeedIcon />}
        >
            <Stack spacing={1}>
                <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    spacing={1}
                    sx={{
                        alignItems: { xs: 'flex-start', sm: 'center' },
                        justifyContent: 'space-between',
                    }}
                >
                    <Typography variant="body2" sx={{ fontWeight: 800 }}>
                        {degraded
                            ? 'Обнаружена деградация сервиса'
                            : 'Зафиксирован медленный ответ'}
                    </Typography>

                    <Chip
                        size="small"
                        color={degraded ? 'warning' : 'info'}
                        label={`${safeConsecutiveDegradations} из ${safeDegradationThreshold} медленных проверок`}
                    />
                </Stack>

                <Typography variant="body2">
                    Сервис отвечает, но время ответа превышает установленный порог.
                    Это не аварийное отключение, а ухудшение качества работы.
                </Typography>

                <Box>
                    <Stack
                        direction="row"
                        spacing={1}
                        sx={{
                            justifyContent: 'space-between',
                            mb: 0.75,
                        }}
                    >
                        <Typography variant="caption" color="text.secondary">
                            Последний ответ: {lastResponseTimeMs ?? '-'} мс
                        </Typography>

                        <Typography variant="caption" color="text.secondary">
                            Порог: {responseTimeThresholdMs} мс
                        </Typography>
                    </Stack>

                    <LinearProgress
                        variant="determinate"
                        value={progress}
                        color={degraded ? 'warning' : 'info'}
                        sx={{
                            height: 8,
                            borderRadius: 999,
                        }}
                    />
                </Box>
            </Stack>
        </Alert>
    );
}