# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Image of an application workspace: a JDK, Maven, Node and git, Maven settings making the
# TopLogic release repository available, and the entry script that runs the application of the
# workspace as the main process of the container.

FROM eclipse-temurin:21-jdk

ARG MAVEN_VERSION=3.9.11

RUN apt-get update \
	&& apt-get install -y --no-install-recommends ca-certificates curl git nodejs \
	&& rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
		| tar -xz -C /opt \
	&& ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven

ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH

COPY settings.xml /opt/maven/conf/settings.xml
COPY entrypoint.sh /usr/local/bin/tl-factory-entrypoint
RUN chmod 755 /usr/local/bin/tl-factory-entrypoint

CMD ["/usr/local/bin/tl-factory-entrypoint"]
