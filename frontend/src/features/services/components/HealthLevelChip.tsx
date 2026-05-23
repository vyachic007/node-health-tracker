import { Chip } from '@mui/material';
import { getHealthLevelLabel } from '../model/serviceLabels';
import type { HealthLevel } from '../model/serviceTypes';

interface HealthLevelChipProps {
    level: HealthLevel;
}

export function HealthLevelChip({ level }: HealthLevelChipProps) {
    const color = level === 'HEALTHY'
        ? 'success'
        : level === 'DEGRADED' || level === 'UNSTABLE'
            ? 'warning'
            : 'error';

    return (
        <Chip
            label={getHealthLevelLabel(level)}
            color={color}
            size="small"
            variant="outlined"
        />
    );
}