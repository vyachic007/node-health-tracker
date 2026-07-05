function normalizeBackendDate(value: string): Date {
    return new Date(value);
}

export function formatDateTime(value: string | null | undefined): string {
    if (!value) {
        return '—';
    }

    const match = value.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/);

    if (!match) {
        return '—';
    }

    const [, year, month, day, hour, minute, second = '00'] = match;

    return `${day}.${month}.${year}, ${hour}:${minute}:${second}`;
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