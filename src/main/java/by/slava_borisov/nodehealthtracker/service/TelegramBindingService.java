package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;

public interface TelegramBindingService {

    TelegramBindLinkResponse createCurrentUserBindLink();
}