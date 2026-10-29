#!/bin/sh
# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Main process of an application workspace container: the supervisor of the application.
#
# As long as the file TL_FACTORY_BUILT_MARKER does not exist, the application is not yet built and
# the script only keeps the container alive, so that commands can be run in it. Once the file
# exists, the script runs the application in the directory TL_FACTORY_APP_DIR on the port
# TL_FACTORY_PORT under the context path TL_FACTORY_CONTEXT_PATH (default: / followed by the name of
# the application directory, the artifact). Starting the container thus starts the application,
# stopping the container (SIGTERM) stops the application gracefully and ends the script.
#
# In between, the command tl-workspace-app stops and starts the application through the state
# directory TL_WORKSPACE_APP_STATE, without ending the container:
#
#   desired     Written by tl-workspace-app: "run" or "stop". The supervisor starts or stops the
#               application to match it, within a second.
#   state       Written by the supervisor: "unbuilt", "starting", "running", "stopping", "stopped"
#               or "failed" (ended by itself). "running" means the process runs, not that it
#               already answers.
#   runs        Written by the supervisor: the number of starts so far.
#   pid         The process ID of the application (the Maven JVM) while it runs.
#   exit        The exit code of the last run, once it has ended.
#   app.log     The output of the current run. The same output goes to the standard output of the
#               container, so that the container log shows the output of all runs.
#
# An application that ends on its own (not on request) has failed and is not started again: the
# supervisor sets "state" to "failed" and "desired" to "stop" and keeps the container alive, so that
# it can be repaired and started again. An application whose initialization fails does not end by
# itself - its server keeps running and answers with errors -, so the supervisor recognizes it by
# its log: the server reports READY_MARKER only after the application reported
# STARTED_MARKER. A run whose log shows the server ready but not the application for
# TL_WORKSPACE_APP_START_GRACE seconds (default 5) is stopped and failed as well.
#
# The container runs with an init process as PID 1 (Docker's --init), which reaps orphaned
# processes and forwards the stop signal to this script.

set -eu

STATE="$TL_WORKSPACE_APP_STATE"
STOP_TIMEOUT="${TL_WORKSPACE_APP_STOP_TIMEOUT:-60}"
START_GRACE="${TL_WORKSPACE_APP_START_GRACE:-5}"
STARTED_MARKER='up and running'
READY_MARKER='Server started:'
PREFIX='[tl-workspace-app]'
CONTEXT_PATH="${TL_FACTORY_CONTEXT_PATH:-/$(basename "$TL_FACTORY_APP_DIR")}"
URL="http://localhost:$TL_FACTORY_PORT$CONTEXT_PATH/"

terminating=false
started=false
grace=0
trap 'terminating=true' TERM INT

# Writes a state file atomically, so that a reader never sees it half written.
put() {
	printf '%s\n' "$2" > "$STATE/$1.tmp"
	mv "$STATE/$1.tmp" "$STATE/$1"
}

get() {
	cat "$STATE/$1" 2>/dev/null || true
}

# Waits a second; a signal ends the wait early.
nap() {
	sleep 1 &
	wait $! || true
}

say() {
	echo "$PREFIX $*"
}

start_app() {
	rm -f "$STATE/exit" "$STATE/pid"
	: > "$STATE/app.log"
	# The state changes before the number of runs: who sees the new number reads the new state.
	put state starting
	put runs $(($(get runs) + 1))
	started=false
	grace=0
	say "Starting the application in $TL_FACTORY_APP_DIR on port $TL_FACTORY_PORT under $CONTEXT_PATH."
	# The runner copies the output to the container output and to the log of the run, records the
	# process ID of the application and, once it has ended, its exit code. The arguments of the
	# launch (exec.args) replace the ones the parent POM configures, so that the port and the context
	# path also apply to an application on a TopLogic version whose parent has no property for the
	# context path (tl.contextPath).
	(
		tee -a "$STATE/app.log" < "$STATE/output" &
		copier=$!
		rc=0
		if cd "$TL_FACTORY_APP_DIR"; then
			mvn -B "-Dtl.port=$TL_FACTORY_PORT" "-Dtl.contextPath=$CONTEXT_PATH" \
				"-Dexec.args=-port $TL_FACTORY_PORT -contextPath $CONTEXT_PATH" > "$STATE/output" 2>&1 &
			app=$!
			put pid "$app"
			wait "$app" || rc=$?
		else
			rc=1
			: > "$STATE/output"
		fi
		wait "$copier" || true
		put exit "$rc"
		rm -f "$STATE/pid"
	) &
	# The process ID is known once the runner has started the application.
	while [ ! -f "$STATE/pid" ] && [ ! -f "$STATE/exit" ]; do
		nap
	done
	put state running
}

# Stops the application gracefully and waits until it has ended; after STOP_TIMEOUT seconds it is
# killed. The state afterwards is the first argument, the second one tells why it is stopped.
stop_app() {
	final="$1"
	put state stopping
	say "Stopping the application $2."
	pid="$(get pid)"
	if [ -n "$pid" ]; then
		kill -TERM "$pid" 2>/dev/null || true
	fi
	waited=0
	while [ ! -f "$STATE/exit" ]; do
		if [ "$waited" -ge "$STOP_TIMEOUT" ] && [ -n "$pid" ]; then
			say "The application did not stop within $STOP_TIMEOUT seconds, killing it."
			kill -KILL "$pid" 2>/dev/null || true
			pid=''
		fi
		nap
		waited=$((waited + 1))
	done
	say "The application stopped with exit code $(get exit)."
	put state "$final"
}

# Whether the log of the current run shows that the application failed to initialize, see above.
start_failed() {
	if $started; then
		return 1
	fi
	if grep -q "$STARTED_MARKER" "$STATE/app.log"; then
		started=true
		say "The application is up at $URL."
		return 1
	fi
	if grep -q "$READY_MARKER" "$STATE/app.log"; then
		grace=$((grace + 1))
		[ "$grace" -gt "$START_GRACE" ]
		return
	fi
	return 1
}

rm -rf "$STATE"
mkdir -p "$STATE"
mkfifo "$STATE/output"
# The lock of tl-workspace-app, usable by every user that runs commands in the workspace.
: > "$STATE/lock"
chmod 666 "$STATE/lock"
put runs 0
put desired run
put state unbuilt
echo $$ > "$STATE/supervisor.pid"

while :; do
	state="$(get state)"
	if $terminating; then
		if [ "$state" = running ]; then
			stop_app stopped "because the container stops"
		fi
		exit 0
	fi
	case "$state" in
	unbuilt)
		if [ -f "$TL_FACTORY_BUILT_MARKER" ]; then
			put state stopped
			continue
		fi
		;;
	running)
		if [ -f "$STATE/exit" ]; then
			say "The application ended with exit code $(get exit). Start it again with: tl-workspace-app start"
			put desired stop
			put state failed
			continue
		elif [ "$(get desired)" = stop ]; then
			stop_app stopped "on request"
			continue
		elif start_failed; then
			say "The application failed to start: its server runs, the application did not start. Start it again with: tl-workspace-app start"
			put desired stop
			stop_app failed "that failed to start"
			continue
		fi
		;;
	stopped|failed)
		if [ "$(get desired)" = run ]; then
			start_app
			continue
		fi
		;;
	esac
	nap
done
