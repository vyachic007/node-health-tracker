package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetConfirmRequest;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetRequest;

public interface PasswordResetService {

    void requestPasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);
}