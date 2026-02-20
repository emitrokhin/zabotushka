package ru.mitrohinayulya.zabotushka.service.platform;

/// Contract for registering a webhook with a messenger platform.
///
/// Implementations are responsible for calling the platform-specific API
/// to set the webhook URL that the platform will use to deliver updates.
public interface MessengerWebhookRegistrar {

    /// Registers the application webhook with the messenger platform.
    ///
    /// Called on startup to ensure the platform is configured to send
    /// incoming updates to this application's endpoint.
    void registerWebhook();

    /// Unregisters the application webhook with the messenger platform.
    ///
    /// Called on graceful shutdown to ensure the platform is not configured to send
    /// incoming updates to this application's endpoint.
    void unregisterWebhook();
}
