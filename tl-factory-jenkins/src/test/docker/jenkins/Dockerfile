# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# The Jenkins of tl-factory-jenkins: the pinned LTS release with the plugins of plugins.txt, without
# setup wizard. The init script sets it up from the environment of the container: the
# administrator, its API token, the credential of the git hosting, the executors. Updating Jenkins:
# see the developer article factory/ci, section "Updating Jenkins".
#
# Two targets:
# - ci: the Jenkins of the integration tests of tl-factory-jenkins (JenkinsTestServer).
# - devel: the Jenkins of the local development setup of tl-factory-app (tl-factory-app/devel), which
#   builds the applications of the projects: in addition Maven with the Maven settings of the
#   workspace image of tl-factory, which make the TopLogic release repository available. Built
#   with BuildKit and the additional build context "workspace-image", the image context of the
#   workspace image (tl-factory/src/main/java/com/top_logic/factory/workspace/app/image).

# renovate: datasource=docker depName=jenkins/jenkins
ARG JENKINS_VERSION=2.580.1-lts-jdk21

FROM jenkins/jenkins:${JENKINS_VERSION} AS ci

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# The suffix .override makes the start script of the image copy the init script into an existing
# Jenkins home as well, so that the home always runs the script of the image.
COPY init.groovy.d/tl-factory.groovy /usr/share/jenkins/ref/init.groovy.d/tl-factory.groovy.override

FROM ci AS devel

# renovate: datasource=maven depName=org.apache.maven:apache-maven
ARG MAVEN_VERSION=3.9.11
ARG REPOSITORY_URL=https://dev.top-logic.com/nexus/repository/toplogic/

USER root

# Maven with the SHA-512 checksum published beside it; the JDK is the one of the base image.
RUN file="apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
	&& url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/${file}" \
	&& cd /tmp \
	&& curl -fsSLO "${url}" \
	&& echo "$(curl -fsSL "${url}.sha512" | cut -d ' ' -f 1)  ${file}" | sha512sum -c - \
	&& tar -xzf "${file}" -C /opt \
	&& rm "${file}" \
	&& ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven \
	&& ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

COPY --from=workspace-image settings.xml /opt/maven/conf/settings.xml
RUN sed -i "s|@REPOSITORY_URL@|${REPOSITORY_URL}|g" /opt/maven/conf/settings.xml

ENV MAVEN_HOME=/opt/maven

USER jenkins
