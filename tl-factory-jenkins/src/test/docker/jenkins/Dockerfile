# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# The Jenkins of the integration tests of tl-factory-jenkins (JenkinsTestServer): the pinned LTS
# release with the plugins of plugins.txt, without setup wizard. The init script creates the
# administrator, its API token and the credential of the git hosting from the environment of the
# container. Updating Jenkins: see the developer article factory/ci, section "Updating Jenkins".

# renovate: datasource=docker depName=jenkins/jenkins
ARG JENKINS_VERSION=2.580.1-lts-jdk21

FROM jenkins/jenkins:${JENKINS_VERSION}

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
