import { Chip } from '@mui/material';
import type { IncidentStatus } from '../model/incidentTypes';
import {
    getIncidentStatusColor,
    incidentStatusLabels,
} from '../model/incidentLabels';

interface IncidentStatusChipProps {
    status: IncidentStatus;
}

export function IncidentStatusChip({ status }: IncidentStatusChipProps) {
    return (
        <Chip
            label={incidentStatusLabels[status]}
            color={getIncidentStatusColor(status)}
            size="small"
        />
    );
}