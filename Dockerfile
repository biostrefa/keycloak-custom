
# Production-ready, optimized Keycloak 26.4 image with:
# - custom provider JARs
# - custom login theme
#
# Build context example:
# .
# ├── Dockerfile
# ├── webhooks/
# │   ├── my-spi-1.jar
# │   └── my-spi-2.jar
# └── themes/
#     └── avizi-theme/
#         ├── login/
#         ├── account/        # optional
#         ├── admin/          # optional
#         ├── email/          # optional
#         └── theme.properties

ARG KEYCLOAK_VERSION=26.4.0

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

# Build-time options that should be baked into the optimized image
ENV KC_HEALTH_ENABLED=true \
    KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak

# ---- Custom providers (JARs) ----
# Keycloak expects provider JARs in /opt/keycloak/providers and they must be present BEFORE kc.sh build
COPY --chown=keycloak:keycloak --chmod=644 webhooks/*.jar /opt/keycloak/providers/

# Workaround for Docker timestamp issues that can make Keycloak think provider JARs changed at runtime
# Use a fixed epoch timestamp (seconds since 1970-01-01) so build & runtime match
ARG PROVIDERS_TIMESTAMP=1743465600
RUN touch -m --date=@${PROVIDERS_TIMESTAMP} /opt/keycloak/providers/*.jar

# ---- Custom theme ----
# Copy your theme folder into /opt/keycloak/themes/<theme-name>
COPY --chown=keycloak:keycloak themes/avizi-theme /opt/keycloak/themes/avizi-theme

# Build an optimized server image including providers/themes
RUN /opt/keycloak/bin/kc.sh build

# ---- Final runtime image ----
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}

COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Keep runtime config via env at deploy time (examples below are common, adjust to your setup)
ENV KC_HEALTH_ENABLED=true \
    KC_METRICS_ENABLED=true

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
# In production you typically run: `start --optimized`
# Start the optimized server with local cache (no clustering)
CMD ["start", "--optimized", "--cache=local"]
