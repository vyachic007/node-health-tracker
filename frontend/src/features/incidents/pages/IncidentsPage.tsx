import {
    Alert,
    Box,
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
import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { incidentsApi } from '../api/incidentsApi';
import { IncidentCard } from '../components/IncidentCard';
import { IncidentDetailsDialog } from '../components/IncidentDetailsDialog';
import { IncidentsSummaryCards } from '../components/IncidentsSummaryCards';
import type { Incident, IncidentSeverity, IncidentStatus } from '../model/incidentTypes';

type IncidentFilter = 'ALL' | IncidentStatus | 'HIGH_SEVERITY';
type IncidentSort = 'OPENED_DESC' | 'OPENED_ASC' | 'SEVERITY_DESC';

const severityRank: Record<IncidentSeverity, number> = {
    LOW: 1,
    MEDIUM: 2,
    HIGH: 3,
    CRITICAL: 4,
};

function filterIncidents(incidents: Incident[], filter: IncidentFilter) {
    switch (filter) {
        case 'OPEN':
            return incidents.filter((incident) => incident.status === 'OPEN');

        case 'RESOLVED':
            return incidents.filter((incident) => incident.status === 'RESOLVED');

        case 'HIGH_SEVERITY':
            return incidents.filter(
                (incident) => incident.severity === 'HIGH' || incident.severity === 'CRITICAL',
            );

        case 'ALL':
        default:
            return incidents;
    }
}

function sortIncidents(incidents: Incident[], sort: IncidentSort) {
    const copy = [...incidents];

    switch (sort) {
        case 'OPENED_ASC':
            return copy.sort(
                (a, b) => new Date(a.openedAt).getTime() - new Date(b.openedAt).getTime(),
            );

        case 'SEVERITY_DESC':
            return copy.sort((a, b) => severityRank[b.severity] - severityRank[a.severity]);

        case 'OPENED_DESC':
        default:
            return copy.sort(
                (a, b) => new Date(b.openedAt).getTime() - new Date(a.openedAt).getTime(),
            );
    }
}

export function IncidentsPage() {
    const [filter, setFilter] = useState<IncidentFilter>('ALL');
    const [sort, setSort] = useState<IncidentSort>('OPENED_DESC');
    const [search, setSearch] = useState('');
    const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);

    const {
        data: incidents = [],
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['incidents', 'my'],
        queryFn: incidentsApi.getMyIncidents,
        refetchInterval: 10000,
    });

    const visibleIncidents = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();

        const searched = normalizedSearch
            ? incidents.filter((incident) => {
                const target = `${incident.id} ${incident.serviceName} ${incident.reason} ${incident.status} ${incident.severity}`.toLowerCase();

                return target.includes(normalizedSearch);
            })
            : incidents;

        return sortIncidents(filterIncidents(searched, filter), sort);
    }, [incidents, filter, sort, search]);

    if (isLoading) {
        return <LinearProgress />;
    }

    if (isError) {
        return <Alert severity="error">Не удалось загрузить инциденты.</Alert>;
    }

    return (
        <Stack spacing={3}>
            <Box>
                <Typography variant="h4">
                    Инциденты
                </Typography>

                <Typography color="text.secondary">
                    Открытые и закрытые инциденты с критичностью, историей событий, чек-листом восстановления и анализом повторяемости.
                </Typography>
            </Box>

            <IncidentsSummaryCards incidents={incidents} />

            <Stack
                direction={{ xs: 'column', lg: 'row' }}
                spacing={2}
                sx={{ alignItems: { xs: 'stretch', lg: 'center' } }}
            >
                <TextField
                    label="Поиск"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="ID, сервис, причина, статус или критичность"
                    fullWidth
                />

                <FormControl fullWidth>
                    <InputLabel>Фильтр</InputLabel>

                    <Select
                        label="Фильтр"
                        value={filter}
                        onChange={(event) => setFilter(event.target.value as IncidentFilter)}
                    >
                        <MenuItem value="ALL">Все инциденты</MenuItem>
                        <MenuItem value="OPEN">Открытые</MenuItem>
                        <MenuItem value="RESOLVED">Закрытые</MenuItem>
                        <MenuItem value="HIGH_SEVERITY">Высокая критичность</MenuItem>
                    </Select>
                </FormControl>

                <FormControl fullWidth>
                    <InputLabel>Сортировка</InputLabel>

                    <Select
                        label="Сортировка"
                        value={sort}
                        onChange={(event) => setSort(event.target.value as IncidentSort)}
                    >
                        <MenuItem value="OPENED_DESC">Сначала новые</MenuItem>
                        <MenuItem value="OPENED_ASC">Сначала старые</MenuItem>
                        <MenuItem value="SEVERITY_DESC">Сначала критичные</MenuItem>
                    </Select>
                </FormControl>
            </Stack>

            <Typography color="text.secondary">
                Показано инцидентов: {visibleIncidents.length} из {incidents.length}
            </Typography>

            <Grid container spacing={2}>
                {visibleIncidents.map((incident) => (
                    <Grid key={incident.id} size={{ xs: 12, md: 6, xl: 4 }}>
                        <IncidentCard
                            incident={incident}
                            onOpenDetails={(selected) => setSelectedIncident(selected)}
                        />
                    </Grid>
                ))}
            </Grid>

            {incidents.length === 0 && (
                <Alert severity="info">
                    Инцидентов пока нет. Они появятся после неуспешных проверок сервисов.
                </Alert>
            )}

            {incidents.length > 0 && visibleIncidents.length === 0 && (
                <Alert severity="info">
                    По выбранным фильтрам инциденты не найдены.
                </Alert>
            )}

            <IncidentDetailsDialog
                open={Boolean(selectedIncident)}
                incident={selectedIncident}
                onClose={() => setSelectedIncident(null)}
            />
        </Stack>
    );
}