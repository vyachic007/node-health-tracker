package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookRequest;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookResponse;

public interface TelegramBindingService {

    TelegramBindLinkResponse createCurrentUserBindLink();

    TelegramWebhookResponse handleTelegramWebhook(TelegramWebhookRequest request);
}