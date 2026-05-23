import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Stack,
    TextField,
} from '@mui/material';
import { useState, type FormEvent } from 'react';
import type { CreateNetworkNodeRequest } from '../model/nodeTypes';

interface CreateNodeDialogProps {
    open: boolean;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: (payload: CreateNetworkNodeRequest) => void;
}

export function CreateNodeDialog({
                                     open,
                                     isSubmitting,
                                     onClose,
                                     onSubmit,
                                 }: CreateNodeDialogProps) {
    const [name, setName] = useState('');
    const [host, setHost] = useState('');
    const [description, setDescription] = useState('');

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        onSubmit({
            name: name.trim(),
            host: host.trim(),
            description: description.trim() || null,
        });
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
            <Box component="form" onSubmit={handleSubmit}>
                <DialogTitle>Добавить узел</DialogTitle>

                <DialogContent>
                    <Stack spacing={2.5} sx={{ mt: 1 }}>
                        <TextField
                            label="Название узла"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            placeholder="Например: RuTube"
                            required
                            fullWidth
                        />

                        <TextField
                            label="Адрес узла"
                            value={host}
                            onChange={(event) => setHost(event.target.value)}
                            placeholder="rutube.ru или 192.168.1.10"
                            required
                            fullWidth
                        />

                        <TextField
                            label="Описание"
                            value={description}
                            onChange={(event) => setDescription(event.target.value)}
                            placeholder="Краткое описание узла"
                            multiline
                            minRows={3}
                            fullWidth
                        />
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleClose}>
                        Отмена
                    </Button>

                    <Button type="submit" variant="contained" disabled={isSubmitting}>
                        {isSubmitting ? 'Добавление...' : 'Добавить'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}