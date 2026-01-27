package com.example.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

public class RegistrationWebhookEventListener implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(RegistrationWebhookEventListener.class);

    private final KeycloakSession session;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Hard-code for start – better to read from SPI config / env / DB later
    private static final String BASE_URL = "https://demo-api.avizi.org/";
    private static final URI WEBHOOK_URI = URI.create(BASE_URL).resolve("/v1/user/new");
    private static final String KEYCLOAK_API_KEY = System.getenv("KEYCLOAK_API_KEY");

    public RegistrationWebhookEventListener(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() != EventType.REGISTER) return;

        try {
            UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
            if (user == null) return;

            // Create JSON payload using string formatting to avoid additional dependencies
            String json = String.format(
                "{\"event\":\"USER_REGISTERED\",\"timestamp\":\"%s\",\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"attributes\":%s,\"realm\":\"%s\"}",
                Instant.now().toString(),
                escapeJson(event.getUserId()),
                escapeJson(user.getUsername()),
                escapeJson(user.getEmail() != null ? user.getEmail() : ""),
                escapeJson(user.getFirstName() != null ? user.getFirstName() : ""),
                escapeJson(user.getLastName() != null ? user.getLastName() : ""),
                mapToJson(user.getAttributes() != null ? user.getAttributes() : Map.of()),
                escapeJson(event.getRealmId())
            );

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(WEBHOOK_URI)
                .header("Content-Type", "application/json");

            if (KEYCLOAK_API_KEY != null && !KEYCLOAK_API_KEY.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + KEYCLOAK_API_KEY);
            }

            HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((resp, ex) -> {
                    if (ex != null) {
                        log.error("Registration webhook failed", ex);
                    } else if (resp.statusCode() >= 300) {
                        log.warnf("Registration webhook failed → %d %s", resp.statusCode(), resp.body());
                    } else {
                        log.info("Registration webhook delivered");
                    }
                });

        } catch (Exception e) {
            log.error("Error sending registration webhook", e);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (adminEvent.getResourceType() != ResourceType.USER || adminEvent.getOperationType() != OperationType.CREATE) {
            return;
        }

        try {
            // For admin events, the userId might be in the resourcePath or representation
            String userId = adminEvent.getResourcePath().substring(adminEvent.getResourcePath().lastIndexOf('/') + 1);
            UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
            if (user == null) return;

            // Create JSON payload using string formatting to avoid additional dependencies
            String json = String.format(
                "{\"event\":\"USER_CREATED\",\"timestamp\":\"%s\",\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"attributes\":%s,\"realm\":\"%s\"}",
                Instant.now().toString(),
                escapeJson(userId),
                escapeJson(user.getUsername()),
                escapeJson(user.getEmail() != null ? user.getEmail() : ""),
                escapeJson(user.getFirstName() != null ? user.getFirstName() : ""),
                escapeJson(user.getLastName() != null ? user.getLastName() : ""),
                mapToJson(user.getAttributes() != null ? user.getAttributes() : Map.of()),
                escapeJson(session.getContext().getRealm().getName())
            );

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(WEBHOOK_URI)
                .header("Content-Type", "application/json");

            if (KEYCLOAK_API_KEY != null && !KEYCLOAK_API_KEY.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + KEYCLOAK_API_KEY);
            }

            HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((resp, ex) -> {
                    if (ex != null) {
                        log.error("Admin user creation webhook failed", ex);
                    } else if (resp.statusCode() >= 300) {
                        log.warnf("Admin user creation webhook failed → %d %s", resp.statusCode(), resp.body());
                    } else {
                        log.info("Admin user creation webhook delivered");
                    }
                });

        } catch (Exception e) {
            log.error("Error sending admin user creation webhook", e);
        }
    }

    @Override
    public void close() {}

    // Helper methods for JSON serialization without external libraries
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String mapToJson(Map<String, ?> map) {
        if (map == null || map.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof java.util.Collection) {
                sb.append(listToJson((java.util.Collection<?>) value));
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(value))).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(java.util.Collection<?> list) {
        if (list == null || list.isEmpty()) return "[]";

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
