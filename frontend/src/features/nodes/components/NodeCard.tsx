import {
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Divider,
    Grid,
    IconButton,
    LinearProgress,
    Stack,
    Tooltip,
    Typography,
} from '@mui/material';
import type { ChipProps } from '@mui/material';
import DnsIcon from '@mui/icons-material/Dns';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
    formatMilliseconds,
    formatPercent,
} from '../../../shared/lib/formatters';
import { servicesApi } from '../../services/api/servicesApi';
import type { HealthLevel, NetworkNode } from '../model/nodeTypes';

interface NodeCardProps {
    node: NetworkNode;
    isDeleting: boolean;
    onEdit: (node: NetworkNode) => void;
    onDelete: (node: NetworkNode) => void;
}

function getAccentColor(level: HealthLevel, openIncidents: number) {
    if (openIncidents > 0) {
        return 'error.main';
    }

    switch (level) {
        case 'HEALTHY':
            return 'success.main';
        case 'DEGRADED':
            return 'warning.main';
        case 'UNSTABLE':
            return 'warning.dark';
        case 'CRITICAL':
            return 'error.main';
        default:
            return 'grey.500';
    }
}

function getStatusLabel(node: NetworkNode) {
    if (!node.isActive) {
        return 'Отключён';
    }

    if (node.openIncidents > 0) {
        return 'Требует внимания';
    }

    switch (node.healthLevel) {
        case 'HEALTHY':
            return 'Работает';
        case 'DEGRADED':
            return 'Снижен';
        case 'UNSTABLE':
            return 'Нестабилен';
        case 'CRITICAL':
            return 'Критический';
        default:
            return 'Неизвестно';
    }
}

function getStatusColor(node: NetworkNode): ChipProps['color'] {
    if (!node.isActive) {
        return 'default';
    }

    if (node.openIncidents > 0) {
        return 'error';
    }

    switch (node.healthLevel) {
        case 'HEALTHY':
            return 'success';
        case 'DEGRADED':
        case 'UNSTABLE':
            return 'warning';
        case 'CRITICAL':
            return 'error';
        default:
            return 'default';
    }
}

function getCheckTypeColor(checkType: string): ChipProps['color'] {
    switch (checkType) {
        case 'HTTPS':
        case 'HTTP':
            return 'success';

        case 'TCP':
        case 'PING':
            return 'info';

        case 'DNS':
        case 'SSL':
            return 'warning';

        default:
            return 'default';
    }
}

function MetricBlock({
    label,
    value,
    color,
}: {
    label: string;
    value: string | number;
    color?: string;
}) {
    return (
        <Box>
            <Typography
                variant="caption"
                color="text.secondary"
                sx={{
                    display: 'block',
                    mb: 0.35,
                    fontWeight: 700,
                }}
            >
                {label}
            </Typography>

            <Typography
                sx={{
                    fontWeight: 900,
                    fontSize: 24,
                    lineHeight: 1.15,
                    color: color ?? 'text.primary',
                }}
            >
                {value}
            </Typography>
        </Box>
    );
}

export function NodeCard({ node, isDeleting, onEdit, onDelete }: NodeCardProps) {
    const accentColor = getAccentColor(node.healthLevel, node.openIncidents);

    const healthScore = Math.max(0, Math.min(100, node.healthScore));
    const availability = Math.max(0, Math.min(100, node.availabilityPercent24h ?? 0));

    const { data: services = [] } = useQuery({
        queryKey: ['services', 'my'],
        queryFn: servicesApi.getMyServices,
    });

    const nodeCheckTypes = Array.from(
        new Set(
            services
                .filter((service) => service.nodeId === node.id)
                .map((service) => service.checkType)
        )
    );

    return (
        <Card
            elevation={0}
            sx={{
                height: '100%',
                position: 'relative',
                overflow: 'hidden',
                borderRadius: 1.5,
                border: 1,
                borderColor: node.openIncidents > 0 ? 'error.light' : 'divider',
                bgcolor: 'background.paper',
                transition: '0.18s ease',
                boxShadow: '0 10px 28px rgba(15, 23, 42, 0.05)',
                '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: '0 14px 34px rgba(15, 23, 42, 0.10)',
                    borderColor: accentColor,
                },
                '&::before': {
                    content: '""',
                    position: 'absolute',
                    left: 0,
                    top: 0,
                    bottom: 0,
                    width: 4,
                    bgcolor: accentColor,
                },
            }}
        >
            <CardContent
                sx={{
                    p: 2.5,
                    pl: 3,
                    '&:last-child': {
                        pb: 2.5,
                    },
                }}
            >
                <Stack spacing={2.2}>
                    <Stack
                        direction="row"
                        spacing={2}
                        sx={{
                            justifyContent: 'space-between',
                            alignItems: 'flex-start',
                        }}
                    >
                        <Box sx={{ minWidth: 0 }}>
                            <Typography
                                variant="h6"
                                noWrap
                                sx={{
                                    fontWeight: 900,
                                    letterSpacing: '-0.02em',
                                }}
                            >
                                {node.name}
                            </Typography>

                            <Typography variant="body2" color="text.secondary" noWrap>
                                {node.host}
                            </Typography>
                        </Box>

                        <Stack direction="row" spacing={0.7} sx={{ alignItems: 'center' }}>
                            <Chip
                                label={getStatusLabel(node)}
                                color={getStatusColor(node)}
                                size="small"
                                variant="outlined"
                                sx={{
                                    minWidth: 110,
                                    borderRadius: 999,
                                    fontWeight: 800,
                                    '& .MuiChip-label': {
                                        px: 1.2,
                                        overflow: 'visible',
                                        textOverflow: 'clip',
                                        whiteSpace: 'nowrap',
                                    },
                                }}
                            />

                            <Tooltip title="Редактировать узел">
                                <span>
                                    <IconButton
                                        color="default"
                                        onClick={() => onEdit(node)}
                                        disabled={isDeleting}
                                        size="small"
                                    >
                                        <EditIcon fontSize="small" />
                                    </IconButton>
                                </span>
                            </Tooltip>
                        </Stack>
                    </Stack>

                    <Grid container spacing={2.2} sx={{ alignItems: 'center' }}>
                        <Grid size={{ xs: 12, sm: 4 }}>
                            <Box
                                sx={{
                                    width: 118,
                                    height: 118,
                                    mx: { xs: 'auto', sm: 0 },
                                    borderRadius: '50%',
                                    display: 'grid',
                                    placeItems: 'center',
                                    background: `conic-gradient(currentColor ${healthScore * 3.6}deg, rgba(148, 163, 184, 0.18) 0deg)`,
                                    color: accentColor,
                                    position: 'relative',
                                    '&::after': {
                                        content: '""',
                                        position: 'absolute',
                                        inset: 11,
                                        borderRadius: '50%',
                                        bgcolor: 'background.paper',
                                    },
                                }}
                            >
                                <Box
                                    sx={{
                                        position: 'relative',
                                        zIndex: 1,
                                        textAlign: 'center',
                                    }}
                                >
                                    <Typography
                                        sx={{
                                            fontSize: 34,
                                            fontWeight: 950,
                                            lineHeight: 1,
                                            color: accentColor,
                                        }}
                                    >
                                        {node.healthScore}
                                    </Typography>

                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        sx={{ fontWeight: 800 }}
                                    >
                                        /100
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{
                                    display: 'block',
                                    mt: 0.75,
                                    fontWeight: 800,
                                }}
                            >
                                Оценка здоровья
                            </Typography>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 8 }}>
                            <Stack spacing={1.4}>
                                <MetricBlock
                                    label="Доступность за 24 часа"
                                    value={formatPercent(node.availabilityPercent24h)}
                                />

                                <LinearProgress
                                    variant="determinate"
                                    value={availability}
                                    sx={{
                                        height: 10,
                                        borderRadius: 999,
                                        bgcolor: 'action.hover',
                                        '& .MuiLinearProgress-bar': {
                                            borderRadius: 999,
                                            bgcolor: accentColor,
                                        },
                                    }}
                                />

                                <Grid container spacing={2}>
                                    <Grid size={12}>
                                        <MetricBlock
                                            label="Средний ответ"
                                            value={formatMilliseconds(node.averageResponseTimeMs24h)}
                                        />
                                    </Grid>
                                </Grid>
                            </Stack>
                        </Grid>
                    </Grid>

                    <Box>
                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                display: 'block',
                                mb: 1,
                                fontWeight: 800,
                            }}
                        >
                            Проверки в этом узле
                        </Typography>

                        <Stack
                            direction="row"
                            spacing={1}
                            useFlexGap
                            sx={{
                                flexWrap: 'wrap',
                                alignItems: 'center',
                            }}
                        >
                            {nodeCheckTypes.length > 0 ? (
                                nodeCheckTypes.map((checkType) => (
                                    <Chip
                                        key={checkType}
                                        label={checkType}
                                        color={getCheckTypeColor(checkType)}
                                        size="small"
                                        variant="outlined"
                                        sx={{
                                            height: 30,
                                            minWidth: 88,
                                            borderRadius: 999,
                                            justifyContent: 'center',
                                            fontWeight: 850,
                                            '& .MuiChip-label': {
                                                px: 1.4,
                                                overflow: 'visible',
                                                textOverflow: 'clip',
                                                whiteSpace: 'nowrap',
                                            },
                                        }}
                                    />
                                ))
                            ) : (
                                <Chip
                                    label="Проверки не добавлены"
                                    size="small"
                                    variant="outlined"
                                    sx={{
                                        height: 30,
                                        borderRadius: 999,
                                        fontWeight: 800,
                                    }}
                                />
                            )}
                        </Stack>
                    </Box>

                    {node.description && (
                        <Typography variant="body2" color="text.secondary">
                            {node.description}
                        </Typography>
                    )}

                    <Divider />

                    <Grid
                        container
                        spacing={1}
                        sx={{
                            p: 1.4,
                            borderRadius: 1.5,
                            bgcolor: 'background.default',
                            border: 1,
                            borderColor: 'divider',
                        }}
                    >
                        <Grid size={3}>
                            <Typography
                                color="text.secondary"
                                sx={{ fontSize: 18, fontWeight: 500 }}
                            >
                                Работают
                            </Typography>

                            <Typography sx={{ fontWeight: 900, fontSize: 26, color: 'success.main' }}>
                                {node.upServices}
                            </Typography>
                        </Grid>

                        <Grid size={3}>
                            <Typography
                                color="text.secondary"
                                sx={{ fontSize: 18, fontWeight: 500 }}
                            >
                                Недоступны
                            </Typography>

                            <Typography
                                sx={{
                                    fontWeight: 900,
                                    fontSize: 26,
                                    color: node.downServices > 0 ? 'error.main' : 'text.primary',
                                }}
                            >
                                {node.downServices}
                            </Typography>
                        </Grid>

                        <Grid size={3}>
                            <Typography
                                color="text.secondary"
                                sx={{ fontSize: 18, fontWeight: 500 }}
                            >
                                Всего
                            </Typography>

                            <Typography sx={{ fontWeight: 900, fontSize: 26 }}>
                                {node.totalServices}
                            </Typography>
                        </Grid>

                        <Grid size={3}>
                            <Typography
                                color="text.secondary"
                                sx={{ fontSize: 18, fontWeight: 500 }}
                            >
                                Инциденты
                            </Typography>

                            <Typography
                                sx={{
                                    fontWeight: 900,
                                    fontSize: 26,
                                    color: node.openIncidents > 0 ? 'error.main' : 'success.main',
                                }}
                            >
                                {node.openIncidents}
                            </Typography>
                        </Grid>
                    </Grid>

                    <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                        <Tooltip title="Показать только сервисы этого узла">
                            <span style={{ flex: 1 }}>
                                <Button
                                    component={Link}
                                    to={`/services?nodeId=${node.id}`}
                                    variant="outlined"
                                    startIcon={<DnsIcon />}
                                    fullWidth
                                    sx={{
                                        height: 42,
                                        borderRadius: 2,
                                        fontWeight: 800,
                                    }}
                                >
                                    Сервисы узла
                                </Button>
                            </span>
                        </Tooltip>

                        <Tooltip title="Удалить узел">
                            <span>
                                <IconButton
                                    color="error"
                                    onClick={() => onDelete(node)}
                                    disabled={isDeleting}
                                    sx={{
                                        width: 42,
                                        height: 42,
                                        border: 1,
                                        borderColor: 'error.light',
                                        bgcolor: 'background.paper',
                                    }}
                                >
                                    <DeleteIcon />
                                </IconButton>
                            </span>
                        </Tooltip>
                    </Stack>
                </Stack>
            </CardContent>
        </Card>
    );
}
