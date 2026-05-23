import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    InputLabel,
    LinearProgress,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useMemo, useState } from 'react';
import { nodesApi } from '../api/nodesApi';
import { CreateNodeDialog } from '../components/CreateNodeDialog';
import { EditNodeDialog } from '../components/EditNodeDialog';
import { NodeCard } from '../components/NodeCard';
import { NodesSummaryCards } from '../components/NodesSummaryCards';
import type {
    CreateNetworkNodeRequest,
    NetworkNode,
    UpdateNetworkNodeRequest,
} from '../model/nodeTypes';

type NodeFilter = 'ALL' | 'ACTIVE' | 'INACTIVE' | 'CRITICAL' | 'INCIDENTS';
type NodeSort = 'NAME_ASC' | 'HEALTH_ASC' | 'HEALTH_DESC' | 'LAST_CHECKED_DESC';

function filterNodes(nodes: NetworkNode[], filter: NodeFilter) {
    switch (filter) {
        case 'ACTIVE':
            return nodes.filter((node) => node.isActive);
        case 'INACTIVE':
            return nodes.filter((node) => !node.isActive);
        case 'CRITICAL':
            return nodes.filter((node) => node.healthLevel === 'CRITICAL');
        case 'INCIDENTS':
            return nodes.filter((node) => node.openIncidents > 0);
        case 'ALL':
        default:
            return nodes;
    }
}

function sortNodes(nodes: NetworkNode[], sort: NodeSort) {
    const copy = [...nodes];

    switch (sort) {
        case 'NAME_ASC':
            return copy.sort((a, b) => a.name.localeCompare(b.name, 'ru'));

        case 'HEALTH_ASC':
            return copy.sort((a, b) => a.healthScore - b.healthScore);

        case 'HEALTH_DESC':
            return copy.sort((a, b) => b.healthScore - a.healthScore);

        case 'LAST_CHECKED_DESC':
            return copy.sort((a, b) => {
                const aTime = a.lastCheckedAt ? new Date(a.lastCheckedAt).getTime() : 0;
                const bTime = b.lastCheckedAt ? new Date(b.lastCheckedAt).getTime() : 0;

                return bTime - aTime;
            });

        default:
            return copy;
    }
}

export function NodesPage() {
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [nodeToEdit, setNodeToEdit] = useState<NetworkNode | null>(null);
    const [nodeToDelete, setNodeToDelete] = useState<NetworkNode | null>(null);
    const [deletingNodeId, setDeletingNodeId] = useState<number | null>(null);

    const [filter, setFilter] = useState<NodeFilter>('ALL');
    const [sort, setSort] = useState<NodeSort>('LAST_CHECKED_DESC');
    const [search, setSearch] = useState('');

    const {
        data: nodes = [],
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['nodes', 'my'],
        queryFn: nodesApi.getMyNodes,
    });

    const visibleNodes = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();

        const searched = normalizedSearch
            ? nodes.filter((node) => {
                const target = `${node.name} ${node.host} ${node.description ?? ''}`.toLowerCase();

                return target.includes(normalizedSearch);
            })
            : nodes;

        return sortNodes(filterNodes(searched, filter), sort);
    }, [nodes, filter, sort, search]);

    const createNodeMutation = useMutation({
        mutationFn: nodesApi.createNode,
        onSuccess: () => {
            enqueueSnackbar('Узел добавлен', { variant: 'success' });
            setIsCreateOpen(false);

            queryClient.invalidateQueries({ queryKey: ['nodes', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось добавить узел', { variant: 'error' });
        },
    });

    const updateNodeMutation = useMutation({
        mutationFn: ({
                         nodeId,
                         payload,
                     }: {
            nodeId: number;
            payload: UpdateNetworkNodeRequest;
        }) => nodesApi.updateNode(nodeId, payload),
        onSuccess: () => {
            enqueueSnackbar('Узел обновлён', { variant: 'success' });
            setNodeToEdit(null);

            queryClient.invalidateQueries({ queryKey: ['nodes', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось обновить узел', { variant: 'error' });
        },
    });

    const deleteNodeMutation = useMutation({
        mutationFn: nodesApi.deleteNode,
        onMutate: (nodeId: number) => {
            setDeletingNodeId(nodeId);
        },
        onSuccess: () => {
            enqueueSnackbar('Узел удалён', { variant: 'success' });
            setNodeToDelete(null);

            queryClient.invalidateQueries({ queryKey: ['nodes', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось удалить узел', { variant: 'error' });
        },
        onSettled: () => {
            setDeletingNodeId(null);
        },
    });

    const handleCreateNode = (payload: CreateNetworkNodeRequest) => {
        createNodeMutation.mutate(payload);
    };

    const handleUpdateNode = (nodeId: number, payload: UpdateNetworkNodeRequest) => {
        updateNodeMutation.mutate({ nodeId, payload });
    };

    const handleConfirmDelete = () => {
        if (!nodeToDelete) {
            return;
        }

        deleteNodeMutation.mutate(nodeToDelete.id);
    };

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError) {
        return <Alert severity="error">Не удалось загрузить узлы.</Alert>;
    }

    return (
        <Stack spacing={3}>
            <Stack
                direction={{ xs: 'column', md: 'row' }}
                spacing={2}
                sx={{
                    justifyContent: 'space-between',
                    alignItems: { xs: 'stretch', md: 'flex-start' },
                }}
            >
                <Box>
                    <Typography variant="h4">Узлы</Typography>

                    <Typography color="text.secondary">
                        Управление сетевыми узлами, их состоянием, сервисами и общей оценкой здоровья.
                    </Typography>
                </Box>

                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setIsCreateOpen(true)}
                >
                    Добавить узел
                </Button>
            </Stack>

            <NodesSummaryCards nodes={nodes} />

            <Stack
                direction={{ xs: 'column', lg: 'row' }}
                spacing={2}
                sx={{ alignItems: { xs: 'stretch', lg: 'center' } }}
            >
                <TextField
                    label="Поиск"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Название, адрес или описание"
                    fullWidth
                />

                <FormControl fullWidth>
                    <InputLabel>Фильтр</InputLabel>

                    <Select
                        label="Фильтр"
                        value={filter}
                        onChange={(event) => setFilter(event.target.value as NodeFilter)}
                    >
                        <MenuItem value="ALL">Все узлы</MenuItem>
                        <MenuItem value="ACTIVE">Только активные</MenuItem>
                        <MenuItem value="INACTIVE">Только отключённые</MenuItem>
                        <MenuItem value="CRITICAL">Критические</MenuItem>
                        <MenuItem value="INCIDENTS">С открытыми инцидентами</MenuItem>
                    </Select>
                </FormControl>

                <FormControl fullWidth>
                    <InputLabel>Сортировка</InputLabel>

                    <Select
                        label="Сортировка"
                        value={sort}
                        onChange={(event) => setSort(event.target.value as NodeSort)}
                    >
                        <MenuItem value="LAST_CHECKED_DESC">Сначала последние проверки</MenuItem>
                        <MenuItem value="HEALTH_ASC">Сначала проблемные</MenuItem>
                        <MenuItem value="HEALTH_DESC">Сначала стабильные</MenuItem>
                        <MenuItem value="NAME_ASC">По названию</MenuItem>
                    </Select>
                </FormControl>
            </Stack>

            <Typography color="text.secondary">
                Показано узлов: {visibleNodes.length} из {nodes.length}
            </Typography>

            <Grid container spacing={2}>
                {visibleNodes.map((node) => (
                    <Grid key={node.id} size={{ xs: 12, md: 6, xl: 4 }}>
                        <NodeCard
                            node={node}
                            isDeleting={deletingNodeId === node.id}
                            onEdit={(selectedNode) => setNodeToEdit(selectedNode)}
                            onDelete={(selectedNode) => setNodeToDelete(selectedNode)}
                        />
                    </Grid>
                ))}
            </Grid>

            {nodes.length === 0 && (
                <Alert severity="info">
                    У вас пока нет узлов. Нажмите “Добавить узел”, чтобы создать первый узел мониторинга.
                </Alert>
            )}

            {nodes.length > 0 && visibleNodes.length === 0 && (
                <Alert severity="info">
                    По выбранным фильтрам узлы не найдены.
                </Alert>
            )}

            <CreateNodeDialog
                open={isCreateOpen}
                isSubmitting={createNodeMutation.isPending}
                onClose={() => setIsCreateOpen(false)}
                onSubmit={handleCreateNode}
            />

            <EditNodeDialog
                open={Boolean(nodeToEdit)}
                node={nodeToEdit}
                isSubmitting={updateNodeMutation.isPending}
                onClose={() => setNodeToEdit(null)}
                onSubmit={handleUpdateNode}
            />

            <Dialog
                open={Boolean(nodeToDelete)}
                onClose={() => setNodeToDelete(null)}
                fullWidth
                maxWidth="xs"
            >
                <DialogTitle>Удалить узел?</DialogTitle>

                <DialogContent>
                    <Stack spacing={1}>
                        <Typography>
                            Узел будет удалён из системы мониторинга.
                        </Typography>

                        <Typography sx={{ fontWeight: 800 }}>
                            {nodeToDelete?.name}
                        </Typography>

                        <Alert severity="warning">
                            Если у узла есть сервисы, история проверок или инциденты, backend может запретить удаление из-за связанных данных.
                        </Alert>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2 }}>
                    <Button onClick={() => setNodeToDelete(null)}>
                        Отмена
                    </Button>

                    <Button
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={handleConfirmDelete}
                        disabled={deleteNodeMutation.isPending}
                    >
                        {deleteNodeMutation.isPending ? 'Удаление...' : 'Удалить'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Stack>
    );
}