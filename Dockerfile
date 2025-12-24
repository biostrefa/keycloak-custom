# Custom Keycloak image with keycloak-webhook providers baked in
# Assumes you already downloaded the *-all.jar files into: ./webhooks/

FROM quay.io/keycloak/keycloak:26.4.0

# Copy all provider jars into Keycloak providers directory
COPY ./webhooks/*.jar /opt/keycloak/providers/

# Build an optimized server image layer so Keycloak indexes the providers
RUN /opt/keycloak/bin/kc.sh build

# Start the optimized server
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized"]
