import { format } from 'date-fns';

export function formatDateTime(value: string | null): string {
    if (!value) {
        return '—';
    }

    return format(new Date(value), 'dd.MM.yyyy HH:mm:ss');
}

export function formatPercent(value: number | null): string {
    if (value === null || value === undefined) {
        return '—';
    }

    return `${value.toFixed(2)}%`;
}

export function formatMilliseconds(value: number | null): string {
    if (value === null || value === undefined) {
        return '—';
    }

    return `${value} мс`;
}

export function formatSeconds(value: number | null): string {
    if (value === null || value === undefined) {
        return '—';
    }

    if (value < 60) {
        return `${value} сек.`;
    }

    const minutes = Math.floor(value / 60);
    const seconds = value % 60;

    if (minutes < 60) {
        return `${minutes} мин. ${seconds} сек.`;
    }

    const hours = Math.floor(minutes / 60);
    const restMinutes = minutes % 60;

    return `${hours} ч. ${restMinutes} мин.`;
}