package com.example.keycloak;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RegistrationWebhookEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String ID = "registration-webhook-listener";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new RegistrationWebhookEventListener(session);
    }

    @Override
    public void init(Config.Scope config) {
        // You can read config here if you want dynamic URL etc.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return ID;
    }
}
