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
import PlayCircleFilledIcon from '@mui/icons-material/PlayCircleFilled';
import DeleteIcon from '@mui/icons-material/Delete';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { servicesApi } from '../api/servicesApi';
import { CreateServiceDialog } from '../components/CreateServiceDialog';
import { EditServiceDialog } from '../components/EditServiceDialog';
import { ServiceCard } from '../components/ServiceCard';
import { ServicesStatusChart } from '../components/ServicesStatusChart';
import { ServicesSummaryCards } from '../components/ServicesSummaryCards';
import type {
    CreateNetworkServiceRequest,
    NetworkService,
    UpdateNetworkServiceRequest,
} from '../model/serviceTypes';

type ServiceFilter = 'ALL' | 'UP' | 'DOWN' | 'INCIDENTS' | 'NOT_CHECKED';
type ServiceSort = 'NAME_ASC' | 'HEALTH_ASC' | 'HEALTH_DESC' | 'LAST_CHECKED_DESC';

function filterServices(services: NetworkService[], filter: ServiceFilter) {
    switch (filter) {
        case 'UP':
            return services.filter((service) => service.lastStatus === 'UP');

        case 'DOWN':
            return services.filter((service) => service.lastStatus === 'DOWN');

        case 'INCIDENTS':
            return services.filter((service) => service.hasOpenIncident);

        case 'NOT_CHECKED':
            return services.filter((service) => !service.lastStatus);

        case 'ALL':
        default:
            return services;
    }
}

function sortServices(services: NetworkService[], sort: ServiceSort) {
    const copy = [...services];

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

function getSelectedNodeId(searchParams: URLSearchParams) {
    const nodeIdParam = searchParams.get('nodeId');

    if (!nodeIdParam) {
        return null;
    }

    const parsedNodeId = Number(nodeIdParam);

    return Number.isFinite(parsedNodeId) && parsedNodeId > 0
        ? parsedNodeId
        : null;
}

export function ServicesPage() {
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();
    const [searchParams] = useSearchParams();

    const selectedNodeId = getSelectedNodeId(searchParams);

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [checkingServiceId, setCheckingServiceId] = useState<number | null>(null);
    const [deletingServiceId, setDeletingServiceId] = useState<number | null>(null);
    const [serviceToDelete, setServiceToDelete] = useState<NetworkService | null>(null);
    const [serviceToEdit, setServiceToEdit] = useState<NetworkService | null>(null);
    const [isCheckingAll, setIsCheckingAll] = useState(false);

    const [filter, setFilter] = useState<ServiceFilter>('ALL');
    const [sort, setSort] = useState<ServiceSort>('LAST_CHECKED_DESC');
    const [search, setSearch] = useState('');

    const {
        data: services = [],
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['services', 'my'],
        queryFn: servicesApi.getMyServices,
    });

    const nodeServices = useMemo(() => {
        if (!selectedNodeId) {
            return services;
        }

        return services.filter((service) => service.nodeId === selectedNodeId);
    }, [services, selectedNodeId]);

    const visibleServices = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();

        const searched = normalizedSearch
            ? nodeServices.filter((service) => {
                const target = `${service.name} ${service.targetHost} ${service.port ?? ''} ${service.path ?? ''}`.toLowerCase();

                return target.includes(normalizedSearch);
            })
            : nodeServices;

        return sortServices(filterServices(searched, filter), sort);
    }, [nodeServices, filter, sort, search]);

    const createServiceMutation = useMutation({
        mutationFn: servicesApi.createService,
        onSuccess: () => {
            enqueueSnackbar('Сервис добавлен', { variant: 'success' });
            setIsCreateOpen(false);

            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось добавить сервис', { variant: 'error' });
        },
    });

    const updateServiceMutation = useMutation({
        mutationFn: ({
                         serviceId,
                         payload,
                     }: {
            serviceId: number;
            payload: UpdateNetworkServiceRequest;
        }) => servicesApi.updateService(serviceId, payload),
        onSuccess: () => {
            enqueueSnackbar('Сервис обновлён', { variant: 'success' });
            setServiceToEdit(null);

            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось обновить сервис', { variant: 'error' });
        },
    });

    const runCheckMutation = useMutation({
        mutationFn: servicesApi.runCheck,
        onMutate: (serviceId: number) => {
            setCheckingServiceId(serviceId);
        },
        onSuccess: () => {
            enqueueSnackbar('Проверка выполнена', { variant: 'success' });

            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось выполнить проверку', { variant: 'error' });
        },
        onSettled: () => {
            setCheckingServiceId(null);
        },
    });

    const deleteServiceMutation = useMutation({
        mutationFn: servicesApi.deleteService,
        onMutate: (serviceId: number) => {
            setDeletingServiceId(serviceId);
        },
        onSuccess: () => {
            enqueueSnackbar('Сервис удалён', { variant: 'success' });
            setServiceToDelete(null);

            queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        },
        onError: () => {
            enqueueSnackbar('Не удалось удалить сервис', { variant: 'error' });
        },
        onSettled: () => {
            setDeletingServiceId(null);
        },
    });

    const handleCreateService = (payload: CreateNetworkServiceRequest) => {
        createServiceMutation.mutate(payload);
    };

    const handleUpdateService = (
        serviceId: number,
        payload: UpdateNetworkServiceRequest,
    ) => {
        updateServiceMutation.mutate({ serviceId, payload });
    };

    const handleRunAllChecks = async () => {
        if (visibleServices.length === 0) {
            enqueueSnackbar('Нет сервисов для проверки', { variant: 'info' });
            return;
        }

        setIsCheckingAll(true);

        try {
            for (const service of visibleServices) {
                await servicesApi.runCheck(service.id);
            }

            enqueueSnackbar('Проверка всех выбранных сервисов выполнена', {
                variant: 'success',
            });

            await queryClient.invalidateQueries({ queryKey: ['services', 'my'] });
            await queryClient.invalidateQueries({ queryKey: ['dashboard', 'my'] });
        } catch {
            enqueueSnackbar('Не удалось проверить все сервисы', { variant: 'error' });
        } finally {
            setIsCheckingAll(false);
        }
    };

    const handleConfirmDelete = () => {
        if (!serviceToDelete) {
            return;
        }

        deleteServiceMutation.mutate(serviceToDelete.id);
    };

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError) {
        return <Alert severity="error">Не удалось загрузить сервисы.</Alert>;
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
                    <Typography variant="h4">
                        {selectedNodeId ? `Сервисы узла №${selectedNodeId}` : 'Сервисы'}
                    </Typography>

                    <Typography color="text.secondary">
                        {selectedNodeId
                            ? 'Показаны только сервисы, которые относятся к выбранному сетевому узлу.'
                            : 'Управление проверками HTTP, HTTPS, TCP, DNS, SSL, Heartbeat и Ping.'}
                    </Typography>
                </Box>

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
                    <Button
                        variant="outlined"
                        startIcon={<PlayCircleFilledIcon />}
                        onClick={handleRunAllChecks}
                        disabled={isCheckingAll || visibleServices.length === 0}
                    >
                        {isCheckingAll ? 'Проверка...' : 'Проверить все'}
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => setIsCreateOpen(true)}
                    >
                        Добавить сервис
                    </Button>
                </Stack>
            </Stack>

            <ServicesSummaryCards services={nodeServices} />

            <ServicesStatusChart services={nodeServices} />

            <Stack
                direction={{ xs: 'column', lg: 'row' }}
                spacing={2}
                sx={{ alignItems: { xs: 'stretch', lg: 'center' } }}
            >
                <TextField
                    label="Поиск"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Название, адрес, порт или путь"
                    fullWidth
                />

                <FormControl fullWidth>
                    <InputLabel>Фильтр</InputLabel>

                    <Select
                        label="Фильтр"
                        value={filter}
                        onChange={(event) => setFilter(event.target.value as ServiceFilter)}
                    >
                        <MenuItem value="ALL">Все сервисы</MenuItem>
                        <MenuItem value="UP">Только работающие</MenuItem>
                        <MenuItem value="DOWN">Только недоступные</MenuItem>
                        <MenuItem value="INCIDENTS">С открытым инцидентом</MenuItem>
                        <MenuItem value="NOT_CHECKED">Не проверялись</MenuItem>
                    </Select>
                </FormControl>

                <FormControl fullWidth>
                    <InputLabel>Сортировка</InputLabel>

                    <Select
                        label="Сортировка"
                        value={sort}
                        onChange={(event) => setSort(event.target.value as ServiceSort)}
                    >
                        <MenuItem value="LAST_CHECKED_DESC">
                            Сначала последние проверки
                        </MenuItem>
                        <MenuItem value="HEALTH_ASC">Сначала проблемные</MenuItem>
                        <MenuItem value="HEALTH_DESC">Сначала стабильные</MenuItem>
                        <MenuItem value="NAME_ASC">По названию</MenuItem>
                    </Select>
                </FormControl>
            </Stack>

            <Typography color="text.secondary">
                {selectedNodeId
                    ? `Показано сервисов выбранного узла: ${visibleServices.length} из ${nodeServices.length}`
                    : `Показано сервисов: ${visibleServices.length} из ${services.length}`}
            </Typography>

            <Grid container spacing={2}>
                {visibleServices.map((service) => (
                    <Grid key={service.id} size={{ xs: 12, md: 6, xl: 4 }}>
                        <ServiceCard
                            service={service}
                            isChecking={checkingServiceId === service.id || isCheckingAll}
                            isDeleting={deletingServiceId === service.id}
                            onRunCheck={(serviceId) => runCheckMutation.mutate(serviceId)}
                            onEdit={(selectedService) => setServiceToEdit(selectedService)}
                            onDelete={(selectedService) => setServiceToDelete(selectedService)}
                        />
                    </Grid>
                ))}
            </Grid>

            {services.length === 0 && (
                <Alert severity="info">
                    У вас пока нет сервисов для мониторинга. Нажмите “Добавить сервис”, чтобы создать первую проверку.
                </Alert>
            )}

            {services.length > 0 && nodeServices.length === 0 && selectedNodeId && (
                <Alert severity="info">
                    У выбранного узла пока нет сервисов.
                </Alert>
            )}

            {nodeServices.length > 0 && visibleServices.length === 0 && (
                <Alert severity="info">
                    По выбранным фильтрам сервисы не найдены.
                </Alert>
            )}

            <CreateServiceDialog
                open={isCreateOpen}
                isSubmitting={createServiceMutation.isPending}
                initialNodeId={selectedNodeId}
                onClose={() => setIsCreateOpen(false)}
                onSubmit={handleCreateService}
            />

            <EditServiceDialog
                open={Boolean(serviceToEdit)}
                service={serviceToEdit}
                isSubmitting={updateServiceMutation.isPending}
                onClose={() => setServiceToEdit(null)}
                onSubmit={handleUpdateService}
            />

            <Dialog
                open={Boolean(serviceToDelete)}
                onClose={() => setServiceToDelete(null)}
                fullWidth
                maxWidth="xs"
            >
                <DialogTitle>Удалить сервис?</DialogTitle>

                <DialogContent>
                    <Stack spacing={1}>
                        <Typography>
                            Сервис будет удалён из списка мониторинга.
                        </Typography>

                        <Typography sx={{ fontWeight: 800 }}>
                            {serviceToDelete?.name}
                        </Typography>

                        <Alert severity="warning">
                            Если для сервиса есть история проверок или инциденты, backend может запретить удаление из-за связанных данных.
                        </Alert>
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2 }}>
                    <Button onClick={() => setServiceToDelete(null)}>
                        Отмена
                    </Button>

                    <Button
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={handleConfirmDelete}
                        disabled={deleteServiceMutation.isPending}
                    >
                        {deleteServiceMutation.isPending ? 'Удаление...' : 'Удалить'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Stack>
    );
}