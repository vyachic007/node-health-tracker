import { Card, CardContent, Grid, Stack, Typography } from '@mui/material';
import HubIcon from '@mui/icons-material/Hub';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import type { NetworkNode } from '../model/nodeTypes';

interface NodesSummaryCardsProps {
    nodes: NetworkNode[];
}

export function NodesSummaryCards({ nodes }: NodesSummaryCardsProps) {
    const total = nodes.length;
    const active = nodes.filter((node) => node.isActive).length;
    const critical = nodes.filter((node) => node.healthLevel === 'CRITICAL').length;
    const incidents = nodes.reduce((sum, node) => sum + node.openIncidents, 0);

    const cards = [
        {
            title: 'Всего узлов',
            value: total,
            icon: <HubIcon />,
            color: 'primary.main',
        },
        {
            title: 'Активные',
            value: active,
            icon: <CheckCircleIcon />,
            color: 'success.main',
        },
        {
            title: 'Критические',
            value: critical,
            icon: <ErrorIcon />,
            color: 'error.main',
        },
        {
            title: 'Открытые инциденты',
            value: incidents,
            icon: <ReportProblemIcon />,
            color: 'warning.main',
        },
    ];

    return (
        <Grid container spacing={2}>
            {cards.map((card) => (
                <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                    <Card
                        elevation={0}
                        sx={{
                            height: '100%',
                            border: 1,
                            borderColor: 'divider',
                        }}
                    >
                        <CardContent>
                            <Stack spacing={1.5}>
                                <Stack
                                    direction="row"
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                    }}
                                >
                                    <Typography color="text.secondary" sx={{ fontWeight: 700 }}>
                                        {card.title}
                                    </Typography>

                                    <Stack sx={{ color: card.color }}>
                                        {card.icon}
                                    </Stack>
                                </Stack>

                                <Typography variant="h4">
                                    {card.value}
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}