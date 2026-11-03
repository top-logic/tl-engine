# The image of ${DisplayName}: the runtime image of TopLogic with the application at the root of the
# server. Build it with bin/image.sh, which builds the application for production first.
#
# The runtime image provides everything else: Tomcat as the user toplogic (never root), the data
# volume /var/lib/toplogic, the secrets from the files of /run/secrets, the JVM options of
# operation and the trust in the reverse proxy in front (TopLogic article docker-image).
ARG TL_VERSION=\${tlVersion}
FROM top-logic/tl-app-runtime:${TL_VERSION}

ARG APP=target/\${artifactId}-\${version}-app
COPY --chown=toplogic:toplogic ${APP}/ /usr/local/tomcat/webapps/ROOT/
