# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# The runtime image of a TopLogic application: Apache Tomcat on a Java runtime, hardened for
# operation. An application image adds nothing but the unpacked application, at the root of the
# server:
#
#   FROM top-logic/tl-app-runtime:<TopLogic version>
#   COPY --chown=toplogic:toplogic target/my-app-1.0-app/ /usr/local/tomcat/webapps/ROOT/
#
# Built by build.sh of this folder, see README.md. Base: the official Tomcat image (Apache-2.0) on
# Eclipse Temurin (OpenJDK, GPL-2.0 with the Classpath Exception) and Ubuntu 24.04, pinned by the
# digest of its tag; Renovate moves the digest (see the customManagers in renovate.json).
FROM tomcat:11.0.27-jre21-temurin-noble@sha256:8131057ac8cc2a26f4f526aa3c2810d738ae31c36ba0a52ab5417a89ac6eceef

# The account the application runs as (never root), and its data: the files of the engine
# (tl_storage_path), its logs and the heap dump of an OutOfMemoryError. The database is outside.
#
# Tomcat itself: no shutdown port, no deployment while running, the client address and the
# protocol of a request taken from the reverse proxy in front (X-Forwarded-For,
# X-Forwarded-Proto; trusted from the private networks only, the default of RemoteIpValve), the
# address of the client in the access log (kept 30 days), and no version or stack trace on an
# error page of Tomcat. The build fails when the configuration of a newer Tomcat no longer has the
# places these changes are made at.
RUN useradd --system --uid 10001 --user-group --home-dir /var/lib/toplogic --shell /usr/sbin/nologin toplogic \
	&& mkdir -p /var/lib/toplogic /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/conf/Catalina/localhost \
	&& chown -R toplogic:toplogic /var/lib/toplogic /usr/local/tomcat/webapps /usr/local/tomcat/logs \
		/usr/local/tomcat/work /usr/local/tomcat/temp \
	&& sed -i \
		-e 's|<Server port="8005" shutdown="SHUTDOWN">|<Server port="-1" shutdown="SHUTDOWN">|' \
		-e 's|unpackWARs="true" autoDeploy="true">|unpackWARs="false" autoDeploy="false" deployOnStartup="true">\n        <Valve className="org.apache.catalina.valves.RemoteIpValve" protocolHeader="X-Forwarded-Proto" />\n        <Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />|' \
		-e 's|prefix="localhost_access_log"|prefix="localhost_access_log" requestAttributesEnabled="true" maxDays="30"|' \
		/usr/local/tomcat/conf/server.xml \
	&& grep -q '<Server port="-1"' /usr/local/tomcat/conf/server.xml \
	&& grep -q 'autoDeploy="false"' /usr/local/tomcat/conf/server.xml \
	&& grep -q 'RemoteIpValve' /usr/local/tomcat/conf/server.xml \
	&& grep -q 'requestAttributesEnabled="true"' /usr/local/tomcat/conf/server.xml

# The JVM options (bin/setenv.sh) and the configuration of the installation that the engine loads
# last (tl_config): the secrets of the installation are the files of /run/secrets.
COPY setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY toplogic.config.xml /usr/local/tomcat/conf/toplogic.config.xml

ENV tl_storage_path=/var/lib/toplogic \
	tl_operation_mode=production

USER 10001
VOLUME /var/lib/toplogic
EXPOSE 8080
CMD ["catalina.sh", "run"]
