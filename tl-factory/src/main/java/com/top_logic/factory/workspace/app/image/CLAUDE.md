<!--
SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>

SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
-->

# Workspace of the TopLogic control application

You work in a container of the TopLogic control application holding one TopLogic application in
`$TL_FACTORY_APP_DIR`.

- **The application already runs.** It is the main process of the container, on port 8080 under a
  supervisor, at `http://localhost:8080$TL_FACTORY_CONTEXT_PATH/`: the context path may have
  several segments, e.g. `/preview/<workspace>/<artifactId>`, and `tl-workspace-app status` prints
  the URL. Never start a second instance (`mvn` with the launch, `exec:java`, a Jetty of your own):
  it fails on the locked database.
- **Restart after a build** with `tl-workspace-app restart` (or the `tl-app` skill, which hands
  its commands to it). It prints the URL, the log and the login once the application answers again.
  `tl-workspace-app status|start|stop|log` shows the state, starts, stops (the container keeps
  running) and prints the end of the log.
- **If the application does not start,** `tl-workspace-app status` reports `failed` and
  `tl-workspace-app log` shows why. Fix the cause, then `tl-workspace-app start`.
- **Login:** `root` with the initial password, shown by `tl-workspace-app status` and held in
  `$tl_initial_password`.
- **LLM:** the model is reached through the control application; there are no API keys in the
  container, none are needed.
- **Browser checks:** use the Playwright MCP server (`playwright`) against
  `http://localhost:8080$TL_FACTORY_CONTEXT_PATH/`.
