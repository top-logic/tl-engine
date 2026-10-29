#!/bin/sh
# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Controls the application of an application workspace, which the supervisor (the entry script of
# the container) runs on port TL_FACTORY_PORT under the context path TL_FACTORY_CONTEXT_PATH.
#
# Usage: tl-workspace-app <status|start|stop|restart|log> [<app-module-path>]
#
#   status    Prints "status: <state>" (unbuilt, starting, running, stopping, stopped or failed;
#             "starting" also while the process runs but does not answer yet), the url, log and
#             login lines and, after a run has ended, "exit: <code>". Exits 0 if the application
#             answers, 4 if it failed (ended by itself), 3 otherwise.
#   start     Starts the application if it is stopped or failed and waits until it answers.
#   stop      Stops the application; the container keeps running.
#   restart   Stops the application and starts it again, on the same database.
#   log       Prints the last TL_WORKSPACE_APP_LOG_LINES (default 100) lines of the current run.
#
# On a successful start or restart it prints, like the tl-app skill of tl-claude-tools (which hands
# its commands to this one through TL_APP_CONTROL):
#
#   url: http://localhost:<port><context path>/
#   log: <log of the run>
#   login: root / <initial password>
#
# On failure it prints diagnostics to the standard error and exits 1. start and restart wait at most
# TL_WORKSPACE_APP_TIMEOUT seconds (default 300) for the application to answer.
#
# The module path is optional; if given, it must be the directory of the workspace's application,
# the only one the workspace runs.

set -eu

STATE="$TL_WORKSPACE_APP_STATE"
TIMEOUT="${TL_WORKSPACE_APP_TIMEOUT:-300}"
LOG_LINES="${TL_WORKSPACE_APP_LOG_LINES:-100}"
LOG="$STATE/app.log"
URL="http://localhost:$TL_FACTORY_PORT${TL_FACTORY_CONTEXT_PATH:-/$(basename "$TL_FACTORY_APP_DIR")}/"

usage() {
	echo "Usage: tl-workspace-app <status|start|stop|restart|log> [<app-module-path>]" >&2
	exit 1
}

fail() {
	echo "Error: $*" >&2
	exit 1
}

put() {
	printf '%s\n' "$2" > "$STATE/$1.tmp.$$"
	mv "$STATE/$1.tmp.$$" "$STATE/$1"
}

get() {
	cat "$STATE/$1" 2>/dev/null || true
}

nap() {
	sleep 1
}

# Whether the application answers its URL with a page or a redirect.
answers() {
	code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 "$URL" 2>/dev/null || true)"
	case "$code" in
	2??|3??) return 0 ;;
	*) return 1 ;;
	esac
}

report() {
	echo "url: $URL"
	echo "log: $LOG"
	if [ -n "${tl_initial_password:-}" ]; then
		echo "login: root / $tl_initial_password"
	fi
}

check_supervisor() {
	pid="$(get supervisor.pid)"
	if [ -z "$pid" ] || ! kill -0 "$pid" 2>/dev/null; then
		fail "The supervisor of the application does not run (no state in $STATE)."
	fi
}

check_module() {
	module="$1"
	app="$(cd "$TL_FACTORY_APP_DIR" 2>/dev/null && pwd -P || echo "$TL_FACTORY_APP_DIR")"
	given="$(cd "$module" 2>/dev/null && pwd -P || echo "$module")"
	if [ "$given" != "$app" ]; then
		fail "This workspace runs only the application in $TL_FACTORY_APP_DIR, not $module."
	fi
}

check_built() {
	if [ ! -f "$TL_FACTORY_BUILT_MARKER" ]; then
		fail "The application is not built yet; it starts once $TL_FACTORY_BUILT_MARKER exists."
	fi
}

# Waits until the run with the given number (or a later one) answers.
await_run() {
	run="$1"
	waited=0
	while [ "$waited" -lt "$TIMEOUT" ]; do
		# The number of runs first, see the supervisor.
		runs="$(get runs)"
		state="$(get state)"
		if [ "$runs" -ge "$run" ]; then
			case "$state" in
			running)
				if answers; then
					report
					return 0
				fi
				;;
			failed|stopped)
				echo "Error: The application ended with exit code $(get exit)." >&2
				echo "Log: $LOG" >&2
				tail -n 30 "$LOG" >&2 2>/dev/null || true
				exit 1
				;;
			esac
		fi
		nap
		waited=$((waited + 1))
	done
	echo "Error: Timeout (${TIMEOUT}s) waiting for the application to answer at $URL." >&2
	echo "Log: $LOG" >&2
	tail -n 20 "$LOG" >&2 2>/dev/null || true
	exit 1
}

do_start() {
	check_built
	runs="$(get runs)"
	case "$(get state)" in
	starting|running)
		await_run "$runs"
		;;
	*)
		put desired run
		await_run $((runs + 1))
		;;
	esac
}

do_stop() {
	check_built
	put desired stop
	waited=0
	while :; do
		case "$(get state)" in
		stopped|failed) break ;;
		esac
		if [ "$waited" -ge "$TIMEOUT" ]; then
			fail "Timeout (${TIMEOUT}s) waiting for the application to stop."
		fi
		nap
		waited=$((waited + 1))
	done
	echo "Stopped."
}

do_status() {
	state="$(get state)"
	ok=false
	if [ "$state" = running ]; then
		if answers; then
			ok=true
		else
			state=starting
		fi
	fi
	echo "status: $state"
	report
	if [ -n "$(get exit)" ] && { [ "$state" = stopped ] || [ "$state" = failed ]; }; then
		echo "exit: $(get exit)"
	fi
	if [ "$state" = failed ]; then
		exit 4
	fi
	$ok || exit 3
}

if [ $# -lt 1 ] || [ $# -gt 2 ]; then
	usage
fi
COMMAND="$1"
if [ $# -eq 2 ]; then
	check_module "$2"
fi
check_supervisor

case "$COMMAND" in
status)
	do_status
	;;
log)
	tail -n "$LOG_LINES" "$LOG"
	;;
start|stop|restart)
	# One change at a time.
	exec 9> "$STATE/lock"
	flock 9
	case "$COMMAND" in
	start) do_start ;;
	stop) do_stop ;;
	restart) do_stop; do_start ;;
	esac
	;;
*)
	echo "Error: Unknown command '$COMMAND'." >&2
	usage
	;;
esac
