function normalizeBackendDate(value: string): Date {
    const hasTimezone =
        value.endsWith('Z') ||
        /[+-]\d{2}:\d{2}$/.test(value);

    if (hasTimezone) {
        return new Date(value);
    }

    return new Date(`${value}Z`);
}

export function formatDateTime(value: string | null | undefined): string {
    if (!value) {
        return '—';
    }

    const date = normalizeBackendDate(value);

    if (Number.isNaN(date.getTime())) {
        return '—';
    }

    return new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    }).format(date);
}

export function formatMilliseconds(value: number | null | undefined): string {
    if (value === null || value === undefined) {
        return '—';
    }

    return `${value} мс`;
}

export function formatPercent(value: number | null | undefined): string {
    if (value === null || value === undefined) {
        return '—';
    }

    return `${value.toFixed(2)}%`;
}

export function formatSeconds(value: number | null | undefined): string {
    if (value === null || value === undefined) {
        return '—';
    }

    const totalSeconds = Math.max(0, Math.floor(value));

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
        return `${hours} ч. ${minutes} мин. ${seconds} сек.`;
    }

    if (minutes > 0) {
        return `${minutes} мин. ${seconds} сек.`;
    }

    return `${seconds} сек.`;
}

export function getSecondsUntil(value: string | null | undefined): number | null {
    if (!value) {
        return null;
    }

    const targetDate = normalizeBackendDate(value);
    const diffMs = targetDate.getTime() - Date.now();

    if (Number.isNaN(diffMs)) {
        return null;
    }

    return Math.max(0, Math.floor(diffMs / 1000));
}