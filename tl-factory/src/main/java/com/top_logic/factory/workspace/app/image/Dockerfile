# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Image of an application workspace: a JDK, Maven, Node and git, Maven settings making the
# TopLogic release repository available, Claude Code with the TopLogic plugin and a headless
# Chromium for the Playwright MCP server, and the entry script that runs the application of the
# workspace as the main process of the container.
#
# The entry script and all commands run as the user @WORKSPACE_USER@. The mount points of the
# workspace volumes belong to this user, so that a fresh named volume mounted there belongs to it
# as well. The placeholders @...@ are replaced by ApplicationWorkspaces.imageContext().

FROM eclipse-temurin:21-jdk

# The tool versions are pinned and updated by Renovate through the "# renovate:" comments (see the
# customManagers in renovate.json). The output of Claude Code is parsed, so a new version is
# tested before it is used, see the article factory/index.
# renovate: datasource=maven depName=org.apache.maven:apache-maven
ARG MAVEN_VERSION=3.9.11
# renovate: datasource=node-version depName=node
ARG NODE_VERSION=22.23.4
# renovate: datasource=npm depName=@anthropic-ai/claude-code
ARG CLAUDE_CODE_VERSION=2.1.309
# renovate: datasource=npm depName=@playwright/mcp
ARG PLAYWRIGHT_MCP_VERSION=0.0.83
ARG TL_CLAUDE_TOOLS_REPOSITORY=https://github.com/top-logic/tl-claude-tools.git
# renovate: datasource=git-refs depName=tl-claude-tools packageName=https://github.com/top-logic/tl-claude-tools.git currentValue=main
ARG TL_CLAUDE_TOOLS_COMMIT=aecc9e529a70e0b7b2664575af544964d1b5cbe0
ARG WORKSPACE_UID=1000

RUN apt-get update \
	&& apt-get install -y --no-install-recommends ca-certificates curl git xz-utils \
	&& rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
		| tar -xz -C /opt \
	&& ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven

ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH

# Node with npm from the Node.js distribution: Claude Code needs Node 22.
RUN case "$(dpkg --print-architecture)" in \
		amd64) arch=x64 ;; \
		arm64) arch=arm64 ;; \
		*) echo "Unsupported architecture: $(dpkg --print-architecture)" >&2; exit 1 ;; \
	esac \
	&& file="node-v${NODE_VERSION}-linux-${arch}.tar.xz" \
	&& cd /tmp \
	&& curl -fsSLO "https://nodejs.org/dist/v${NODE_VERSION}/${file}" \
	&& curl -fsSL "https://nodejs.org/dist/v${NODE_VERSION}/SHASUMS256.txt" | grep " ${file}\$" | sha256sum -c - \
	&& tar -xJf "${file}" -C /usr/local --strip-components=1 --no-same-owner \
		--exclude='*/CHANGELOG.md' --exclude='*/README.md' --exclude='*/LICENSE' --exclude='*/share/doc' \
	&& rm "${file}"

# Claude Code makes no requests except to the model: no updates, telemetry, error reports or
# surveys.
ENV DISABLE_AUTOUPDATER=1
ENV CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1
ENV DISABLE_TELEMETRY=1
ENV DISABLE_ERROR_REPORTING=1
ENV CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY=1
ENV CLAUDE_CODE_DISABLE_OFFICIAL_MARKETPLACE_AUTOINSTALL=1

ENV PLAYWRIGHT_BROWSERS_PATH=/opt/ms-playwright

# Only the headless shell of Chromium, the Playwright of the MCP server installs it with its system
# libraries. The MCP server is given its executable, otherwise it asks for the full browser.
RUN npm install -g --no-fund --no-audit \
		"@anthropic-ai/claude-code@${CLAUDE_CODE_VERSION}" \
		"@playwright/mcp@${PLAYWRIGHT_MCP_VERSION}" \
	&& /usr/local/lib/node_modules/@playwright/mcp/node_modules/.bin/playwright install --with-deps --only-shell chromium \
	&& ln -s "$(ls -d "${PLAYWRIGHT_BROWSERS_PATH}"/chromium_headless_shell-*/chrome-*/chrome-headless-shell)" \
		/usr/local/bin/chrome-headless-shell \
	&& /usr/local/bin/chrome-headless-shell --version \
	&& rm -rf /var/lib/apt/lists/* /root/.npm /root/.cache

# The marketplace of the TopLogic plugin, at a fixed commit.
RUN git init -q /opt/tl-claude-tools \
	&& git -C /opt/tl-claude-tools fetch -q --depth 1 "${TL_CLAUDE_TOOLS_REPOSITORY}" "${TL_CLAUDE_TOOLS_COMMIT}" \
	&& git -C /opt/tl-claude-tools checkout -q FETCH_HEAD \
	&& rm -rf /opt/tl-claude-tools/.git

# The base image has a user with the ID the workspace user takes.
RUN if existing="$(getent passwd "${WORKSPACE_UID}" | cut -d: -f1)" && [ -n "${existing}" ]; then \
		userdel -r "${existing}"; \
	fi \
	&& useradd --create-home --uid "${WORKSPACE_UID}" --user-group --shell /bin/bash @WORKSPACE_USER@ \
	&& mkdir -p @VOLUME_PATHS@ \
	&& chown @WORKSPACE_USER@:@WORKSPACE_USER@ @VOLUME_PATHS@

COPY settings.xml /opt/maven/conf/settings.xml
COPY entrypoint.sh /usr/local/bin/tl-factory-entrypoint
RUN chmod 755 /usr/local/bin/tl-factory-entrypoint

USER @WORKSPACE_USER@

RUN claude plugin marketplace add /opt/tl-claude-tools \
	&& claude plugin install tl-claude-tools@toplogic \
	&& claude mcp add --scope user playwright -- playwright-mcp --browser chromium --headless \
		--executable-path /usr/local/bin/chrome-headless-shell --output-dir /tmp/playwright-mcp

CMD ["/usr/local/bin/tl-factory-entrypoint"]
