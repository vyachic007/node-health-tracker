import { Chip } from '@mui/material';
import type { IncidentSeverity } from '../model/incidentTypes';
import { getSeverityColor, severityLabels } from '../model/incidentLabels';

interface SeverityChipProps {
    severity: IncidentSeverity;
}

export function SeverityChip({ severity }: SeverityChipProps) {
    return (
        <Chip
            label={severityLabels[severity]}
            color={getSeverityColor(severity)}
            size="small"
            variant="outlined"
        />
    );
}