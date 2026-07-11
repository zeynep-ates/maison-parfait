package com.zeynepates.maisonparfait.backend.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Temporary stand-in until the notification module exists - logs the
 * email instead of sending one, which is enough to build and test every
 * verification/reset/change flow end to end without real SMTP infra.
 */
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("Email to {} | subject: {} | body:\n{}", to, subject, body);
    }
}
