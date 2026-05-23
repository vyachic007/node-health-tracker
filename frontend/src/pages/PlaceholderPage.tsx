import { Card, CardContent, Stack, Typography } from '@mui/material';

interface PlaceholderPageProps {
    title: string;
    description: string;
}

export function PlaceholderPage({ title, description }: PlaceholderPageProps) {
    return (
        <Stack spacing={3}>
            <div>
                <Typography variant="h4">{title}</Typography>
                <Typography color="text.secondary">{description}</Typography>
            </div>

            <Card elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
                <CardContent>
                    <Typography color="text.secondary">
                        Эта страница будет реализована следующим этапом. Базовая архитектура,
                        авторизация, layout, темы и маршрутизация уже подготовлены.
                    </Typography>
                </CardContent>
            </Card>
        </Stack>
    );
}