import { Chip } from '@mui/material';
import type { HealthLevel, NodeHealthStatus } from '../model/nodeTypes';
import {
    getHealthColor,
    healthLevelLabels,
    nodeHealthStatusLabels,
} from '../model/nodeLabels';

interface NodeHealthChipProps {
    level: HealthLevel | NodeHealthStatus;
}

export function NodeHealthChip({ level }: NodeHealthChipProps) {
    const label =
        level in healthLevelLabels
            ? healthLevelLabels[level as HealthLevel]
            : nodeHealthStatusLabels[level as NodeHealthStatus];

    return (
        <Chip
            label={label}
            color={getHealthColor(level)}
            size="small"
            variant="outlined"
        />
    );
}