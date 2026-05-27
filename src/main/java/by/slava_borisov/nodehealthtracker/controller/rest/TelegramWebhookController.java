package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookRequest;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookResponse;
import by.slava_borisov.nodehealthtracker.service.TelegramBindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final TelegramBindingService telegramBindingService;

    @PostMapping("/webhook")
    public TelegramWebhookResponse handleWebhook(@RequestBody TelegramWebhookRequest request) {
        return telegramBindingService.handleTelegramWebhook(request);
    }
}