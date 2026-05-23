import { Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import HelpIcon from '@mui/icons-material/Help';
import { getServiceStatusLabel } from '../model/serviceLabels';
import type { ServiceStatus } from '../model/serviceTypes';

interface ServiceStatusChipProps {
    status: ServiceStatus | null;
}

export function ServiceStatusChip({ status }: ServiceStatusChipProps) {
    if (status === 'UP') {
        return (
            <Chip
                icon={<CheckCircleIcon />}
                label={getServiceStatusLabel(status)}
                color="success"
                size="small"
            />
        );
    }

    if (status === 'DOWN') {
        return (
            <Chip
                icon={<ErrorIcon />}
                label={getServiceStatusLabel(status)}
                color="error"
                size="small"
            />
        );
    }

    return (
        <Chip
            icon={<HelpIcon />}
            label={getServiceStatusLabel(status)}
            color="default"
            size="small"
        />
    );
}