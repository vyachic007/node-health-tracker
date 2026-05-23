import { Card, CardContent, Stack, Typography, useTheme } from '@mui/material';
import {
    Bar,
    BarChart,
    CartesianGrid,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import type { NetworkService } from '../model/serviceTypes';

interface ServicesStatusChartProps {
    services: NetworkService[];
}

export function ServicesStatusChart({ services }: ServicesStatusChartProps) {
    const theme = useTheme();

    const data = [
        {
            name: 'Работают',
            count: services.filter((service) => service.lastStatus === 'UP').length,
        },
        {
            name: 'Недоступны',
            count: services.filter((service) => service.lastStatus === 'DOWN').length,
        },
        {
            name: 'Не проверялись',
            count: services.filter((service) => !service.lastStatus).length,
        },
        {
            name: 'С инцидентом',
            count: services.filter((service) => service.hasOpenIncident).length,
        },
    ];

    return (
        <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
            <CardContent>
                <Stack spacing={2}>
                    <div>
                        <Typography variant="h6">Распределение сервисов</Typography>
                        <Typography color="text.secondary">
                            Сколько сервисов работает, недоступно или имеет открытый инцидент.
                        </Typography>
                    </div>

                    <div style={{ width: '100%', height: 260 }}>
                        <ResponsiveContainer>
                            <BarChart data={data}>
                                <CartesianGrid strokeDasharray="4 4" stroke={theme.palette.divider} />
                                <XAxis dataKey="name" stroke={theme.palette.text.secondary} />
                                <YAxis allowDecimals={false} stroke={theme.palette.text.secondary} />
                                <Tooltip
                                    contentStyle={{
                                        background: theme.palette.background.paper,
                                        border: `1px solid ${theme.palette.divider}`,
                                        borderRadius: 12,
                                    }}
                                />
                                <Bar dataKey="count" name="Количество" fill={theme.palette.primary.main} radius={[8, 8, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </Stack>
            </CardContent>
        </Card>
    );
}