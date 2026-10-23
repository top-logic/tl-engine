#!/bin/sh
# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Main process of an application workspace container.
#
# As long as the file TL_FACTORY_BUILT_MARKER does not exist, the application is not yet built and
# the script only keeps the container alive, so that commands can be run in it. Once the file
# exists, the script runs the application in the directory TL_FACTORY_APP_DIR in the foreground on
# the port TL_FACTORY_PORT. Starting the container thus starts the application, stopping the
# container stops it.

set -eu

trap 'exit 0' TERM INT

while [ ! -f "$TL_FACTORY_BUILT_MARKER" ]; do
	sleep 1 &
	wait $! || true
done

cd "$TL_FACTORY_APP_DIR"
exec mvn -B "-Dtl.port=$TL_FACTORY_PORT"
