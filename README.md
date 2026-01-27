To preview your Keycloak theme, you need to run Keycloak with the custom theme applied. Here are the steps:

## Build and Run with Docker

### Build the Docker image:

docker build -t keycloak-custom .

### Run Keycloak with your theme:

docker run -p 8055:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  keycloak-custom

### Access the theme preview:
Login page: http://localhost:8055/realms/master/protocol/openid-connect/auth

Admin console: http://localhost:8055/admin (admin/admin)

### Theme Configuration
Theme is configured in theme.properties:

- Parent theme: keycloak (inherits from base)
- Styles: css/style.css
- Locales: English and Polish
- Theme names: oskar-theme for login, account, admin, and email

### Development Workflow
For faster development during theme changes:

- Make changes to your theme files in ./themes/avizi-theme/
- Rebuild and restart the Docker container
- Refresh your browser to see changes

The theme will be automatically applied since it's copied to /opt/keycloak/themes/ in the Docker image.

# Registration Webhook Event Listener Implementation

### Overview
A custom Keycloak event listener has been implemented to automatically send newly registered user data to a backend webhook endpoint. This enables real-time user registration notifications and data synchronization.

### Implementation Details
- **Provider Name**: `registration-webhook-listener`
- **Event Type**: `REGISTER` (user registration)
- **HTTP Method**: POST with JSON payload
- **Delivery**: Asynchronous with error handling and logging

### Files Created
```
keycloak-registration-webhook-spi/
├── pom.xml                                    # Maven configuration
├── src/main/java/com/example/keycloak/
│   ├── RegistrationWebhookEventListener.java             # Main event listener logic
│   └── RegistrationWebhookEventListenerProviderFactory.java # Provider factory
└── src/main/resources/META-INF/services/
    └── org.keycloak.events.EventListenerProviderFactory # Service registration
```

### Webhook Payload Format
```json
{
  "event": "USER_REGISTERED",
  "timestamp": "2026-01-26T14:48:53.123Z",
  "userId": "user-uuid-here",
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "attributes": {},
  "realm": "master"
}
```

### Required Configuration

#### 1. Update Webhook URL
Edit `keycloak-registration-webhook-spi/src/main/java/com/example/keycloak/RegistrationWebhookEventListener.java`:

```java
private static final String WEBHOOK_URL = "https://your-backend.example.com/api/users/webhook";
```

#### 2. Authentication (Optional)
Update the Authorization header if your backend requires authentication:

```java
.header("Authorization", "Bearer your-secret-token-if-needed")
```

#### 3. Build and Deploy Commands
After updating the webhook URL, run these commands to rebuild and deploy:

```bash
# Build the webhook JAR
cd keycloak-registration-webhook-spi
mvn clean package

# Copy the updated JAR to webhooks directory
cp target/keycloak-registration-webhook-event-listener-1.0.0.jar ../webhooks/

# Build the Docker image with the updated JAR
cd ..
docker build -t keycloak-custom .

# Run Keycloak with the webhook listener
docker run -p 8055:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  keycloak-custom
```

The JAR file is automatically copied to `webhooks/keycloak-registration-webhook-event-listener-1.0.0.jar` during the Docker build process.

#### 4. Enable Event Listener in Keycloak
After starting Keycloak, enable the event listener:

1. Go to Admin Console → Realm Settings → Events → Event Listeners
2. Add `registration-webhook-listener` to the list
3. Save configuration

### Adding Additional Webhook Listeners

To create additional webhook listeners for different events (e.g., login events, password updates), follow this pattern:

#### Example: Login Webhook Listener

1. **Create new project directory**:
```bash
mkdir keycloak-login-webhook-spi
cd keycloak-login-webhook-spi
```

2. **Copy and modify files**:
   - Copy `pom.xml` and update artifactId to `keycloak-login-webhook-event-listener`
   - Create `LoginWebhookEventListener.java` (modify event type to `EventType.LOGIN`)
   - Create `LoginWebhookEventListenerProviderFactory.java` (update ID to `login-webhook-listener`)

3. **Update event handling**:
```java
@Override
public void onEvent(Event event) {
    if (event.getType() != EventType.LOGIN) return;  // Changed from REGISTER to LOGIN

    // Handle login event logic here
}
```

4. **Build and deploy**:
```bash
mvn clean package
cp target/keycloak-login-webhook-event-listener-1.0.0.jar ../webhooks/
docker build -t keycloak-custom .
```

5. **Enable in Keycloak**:
   - Add `login-webhook-listener` to Event Listeners in Admin Console

#### Common Event Types You Can Listen For:
- `EventType.LOGIN` - User login
- `EventType.LOGOUT` - User logout
- `EventType.UPDATE_PASSWORD` - Password change
- `EventType.UPDATE_EMAIL` - Email change
- `EventType.UPDATE_PROFILE` - Profile update
- `EventType.SEND_VERIFY_EMAIL` - Email verification sent
- `EventType.SEND_RESET_PASSWORD` - Password reset sent

Each listener can have its own:
- Unique provider ID (e.g., `login-webhook-listener`)
- Different webhook URL endpoint
- Custom JSON payload format
- Specific event type filtering

### Technical Specifications
- **Keycloak Version**: 26.4.0
- **Java Version**: 17
- **Dependencies**: Keycloak SPI modules (provided scope)
- **HTTP Client**: Java 11+ HttpClient (async)
- **JSON Serialization**: Custom implementation (no external dependencies)

### Error Handling
- Failed webhook deliveries are logged with error details
- HTTP status codes ≥ 300 trigger warning logs
- Successful deliveries are logged at info level
- All errors are caught to prevent Keycloak operation disruption

### Development Notes
- Event listener is automatically registered via SPI service file
- Uses async HTTP calls to avoid blocking Keycloak operations
- JSON payload includes all standard user fields and custom attributes
- No external dependencies required beyond Keycloak SPI modules
- Multiple listeners can be enabled simultaneously for different events
