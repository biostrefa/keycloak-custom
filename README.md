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