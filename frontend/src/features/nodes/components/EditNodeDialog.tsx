import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    Stack,
    Switch,
    TextField,
} from '@mui/material';
import { useEffect, useState, type FormEvent } from 'react';
import type { NetworkNode, UpdateNetworkNodeRequest } from '../model/nodeTypes';

interface EditNodeDialogProps {
    open: boolean;
    node: NetworkNode | null;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (nodeId: number, payload: UpdateNetworkNodeRequest) => void;
}

export function EditNodeDialog({
                                   open,
                                   node,
                                   isSubmitting,
                                   onClose,
                                   onSubmit,
                               }: EditNodeDialogProps) {
    const [name, setName] = useState('');
    const [host, setHost] = useState('');
    const [description, setDescription] = useState('');
    const [isActive, setIsActive] = useState(true);

    useEffect(() => {
        if (!node) {
            return;
        }

        setName(node.name);
        setHost(node.host);
        setDescription(node.description ?? '');
        setIsActive(node.isActive);
    }, [node]);

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!node) {
            return;
        }

        onSubmit(node.id, {
            name: name.trim(),
            host: host.trim(),
            description: description.trim() || null,
            isActive,
        });
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <Box component="form" onSubmit={handleSubmit}>
                <DialogTitle>Редактировать узел</DialogTitle>

                <DialogContent>
                    <Stack spacing={2.5} sx={{ mt: 1 }}>
                        <TextField
                            label="Название узла"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                            fullWidth
                        />

                        <TextField
                            label="Адрес узла"
                            value={host}
                            onChange={(event) => setHost(event.target.value)}
                            required
                            fullWidth
                        />

                        <TextField
                            label="Описание"
                            value={description}
                            onChange={(event) => setDescription(event.target.value)}
                            multiline
                            minRows={3}
                            fullWidth
                        />

                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isActive}
                                    onChange={(event) => setIsActive(event.target.checked)}
                                />
                            }
                            label={isActive ? 'Узел активен' : 'Узел отключён'}
                        />
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={onClose}>
                        Отмена
                    </Button>

                    <Button type="submit" variant="contained" disabled={isSubmitting}>
                        {isSubmitting ? 'Сохранение...' : 'Сохранить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}