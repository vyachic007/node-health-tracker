import {
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Grid,
    IconButton,
    Stack,
    Tooltip,
    Typography,
} from '@mui/material';
import DnsIcon from '@mui/icons-material/Dns';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Link } from 'react-router-dom';
import {
    formatDateTime,
    formatMilliseconds,
    formatPercent,
} from '../../../shared/lib/formatters';
import type { NetworkNode } from '../model/nodeTypes';
import { NodeHealthChip } from './NodeHealthChip';

interface NodeCardProps {
    node: NetworkNode;
    isDeleting: boolean;
    onEdit: (node: NetworkNode) => void;
    onDelete: (node: NetworkNode) => void;
}

export function NodeCard({ node, isDeleting, onEdit, onDelete }: NodeCardProps) {
    return (
        <Card
            elevation={0}
            sx={{
                height: '100%',
                border: 1,
                borderColor: node.openIncidents > 0 ? 'error.main' : 'divider',
            }}
        >
            <CardContent>
                <Stack spacing={2.25}>
                    <Stack
                        direction="row"
                        spacing={2}
                        sx={{ justifyContent: 'space-between' }}
                    >
                        <Box sx={{ minWidth: 0 }}>
                            <Typography variant="h6" noWrap>
                                {node.name}
                            </Typography>

                            <Typography variant="body2" color="text.secondary" noWrap>
                                Адрес узла: {node.host}
                            </Typography>
                        </Box>

                        <NodeHealthChip level={node.healthLevel} />
                    </Stack>

                    <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
                        <Chip
                            label={node.isActive ? 'Активен' : 'Отключён'}
                            color={node.isActive ? 'success' : 'default'}
                            size="small"
                            variant="outlined"
                        />

                        <Chip
                            label={`Сервисов: ${node.totalServices}`}
                            size="small"
                            variant="outlined"
                        />

                        {node.openIncidents > 0 && (
                            <Chip
                                label={`Инцидентов: ${node.openIncidents}`}
                                color="error"
                                size="small"
                            />
                        )}
                    </Stack>

                    {node.description && (
                        <Typography variant="body2" color="text.secondary">
                            {node.description}
                        </Typography>
                    )}

                    <Grid container spacing={2}>
                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Оценка здоровья
                            </Typography>

                            <Typography variant="h5">
                                {node.healthScore}/100
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Доступность за 24 часа
                            </Typography>

                            <Typography variant="h5">
                                {formatPercent(node.availabilityPercent24h)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Средний ответ
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatMilliseconds(node.averageResponseTimeMs24h)}
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="caption" color="text.secondary">
                                Последняя проверка
                            </Typography>

                            <Typography sx={{ fontWeight: 800 }}>
                                {formatDateTime(node.lastCheckedAt)}
                            </Typography>
                        </Grid>
                    </Grid>

                    <Grid container spacing={1}>
                        <Grid size={6}>
                            <Typography variant="body2" color="text.secondary">
                                Работают: <b>{node.upServices}</b>
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="body2" color="text.secondary">
                                Недоступны: <b>{node.downServices}</b>
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="body2" color="text.secondary">
                                Включены: <b>{node.enabledServices}</b>
                            </Typography>
                        </Grid>

                        <Grid size={6}>
                            <Typography variant="body2" color="text.secondary">
                                Отключены: <b>{node.disabledServices}</b>
                            </Typography>
                        </Grid>
                    </Grid>

                    <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
                        <Tooltip title="Показать сервисы этого узла">
                            <span style={{ flex: 1 }}>
                                <Button
                                    component={Link}
                                    to={`/services?nodeId=${node.id}`}
                                    variant="outlined"
                                    startIcon={<DnsIcon />}
                                    fullWidth
                                >
                                    Сервисы узла
                                </Button>
                            </span>
                        </Tooltip>

                        <Tooltip title="Редактировать узел">
                            <span>
                                <IconButton
                                    color="default"
                                    onClick={() => onEdit(node)}
                                    disabled={isDeleting}
                                    sx={{
                                        width: 42,
                                        height: 42,
                                        border: 1,
                                        borderColor: 'divider',
                                    }}
                                >
                                    <EditIcon />
                                </IconButton>
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
                                        borderColor: 'error.main',
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