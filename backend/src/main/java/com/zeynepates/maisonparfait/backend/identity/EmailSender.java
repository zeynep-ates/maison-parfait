package com.zeynepates.maisonparfait.backend.identity;

/**
 * Deliberately minimal - real provider integration (and multi-channel
 * support) belongs to the future notification module (architecture doc
 * Phase 9). LoggingEmailSender is the only implementation for now.
 */
public interface EmailSender {
    void send(String to, String subject, String body);
}
