import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    TextField,
} from '@mui/material';
import { useState, type FormEvent } from 'react';
import type { CheckType, CreateNetworkServiceRequest } from '../model/serviceTypes';
import { checkTypeLabels } from '../model/serviceLabels';

const checkTypes: CheckType[] = ['HTTP', 'HTTPS', 'TCP', 'DNS', 'SSL', 'HEARTBEAT', 'PING'];

interface CreateServiceDialogProps {
    open: boolean;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (payload: CreateNetworkServiceRequest) => void;
}

export function CreateServiceDialog({
                                        open,
                                        isSubmitting,
                                        onClose,
                                        onSubmit,
                                    }: CreateServiceDialogProps) {
    const [nodeId, setNodeId] = useState('7');
    const [checkType, setCheckType] = useState<CheckType>('HTTP');
    const [name, setName] = useState('');
    const [targetHost, setTargetHost] = useState('');
    const [port, setPort] = useState('80');
    const [path, setPath] = useState('/');
    const [intervalSeconds, setIntervalSeconds] = useState('3600');

    const handleCheckTypeChange = (value: CheckType) => {
        setCheckType(value);

        if (value === 'HTTP') {
            setPort('80');
            setPath('/');
            return;
        }

        if (value === 'HTTPS') {
            setPort('443');
            setPath('/');
            return;
        }

        if (value === 'SSL') {
            setPort('443');
            setPath('');
            return;
        }

        if (value === 'DNS' || value === 'HEARTBEAT' || value === 'PING') {
            setPort('');
            setPath('');
            return;
        }

        if (value === 'TCP') {
            setPath('');
        }
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        onSubmit({
            nodeId: Number(nodeId),
            checkType,
            name: name.trim(),
            targetHost: targetHost.trim(),
            port: port.trim() ? Number(port) : null,
            path: path.trim() || null,
            intervalSeconds: Number(intervalSeconds),
        });
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <Box component="form" onSubmit={handleSubmit}>
                <DialogTitle>Добавить сервис для мониторинга</DialogTitle>

                <DialogContent>
                    <Stack spacing={2.5} sx={{ mt: 1 }}>
                        <TextField
                            label="ID узла"
                            value={nodeId}
                            onChange={(event) => setNodeId(event.target.value)}
                            helperText="Пока укажи ID существующего узла. Позже заменим на выпадающий список узлов."
                            required
                            fullWidth
                        />

                        <FormControl fullWidth>
                            <InputLabel>Тип проверки</InputLabel>
                            <Select
                                label="Тип проверки"
                                value={checkType}
                                onChange={(event) => handleCheckTypeChange(event.target.value as CheckType)}
                            >
                                {checkTypes.map((type) => (
                                    <MenuItem key={type} value={type}>
                                        {checkTypeLabels[type]}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <TextField
                            label="Название сервиса"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            placeholder="Например: Основной сайт"
                            required
                            fullWidth
                        />

                        <TextField
                            label="Проверяемый адрес"
                            value={targetHost}
                            onChange={(event) => setTargetHost(event.target.value)}
                            placeholder="example.com или 127.0.0.1"
                            required
                            fullWidth
                        />

                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Порт"
                                    value={port}
                                    onChange={(event) => setPort(event.target.value)}
                                    placeholder="80, 443, 5432..."
                                    fullWidth
                                />
                            </Grid>

                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                    label="Путь"
                                    value={path}
                                    onChange={(event) => setPath(event.target.value)}
                                    placeholder="/ или /api/health"
                                    fullWidth
                                />
                            </Grid>
                        </Grid>

                        <TextField
                            label="Интервал проверки, секунд"
                            value={intervalSeconds}
                            onChange={(event) => setIntervalSeconds(event.target.value)}
                            helperText="Например: 60, 300, 3600"
                            required
                            fullWidth
                        />
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={onClose}>Отмена</Button>
                    <Button type="submit" variant="contained" disabled={isSubmitting}>
                        {isSubmitting ? 'Добавление...' : 'Добавить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}