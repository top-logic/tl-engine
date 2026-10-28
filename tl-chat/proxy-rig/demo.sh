#!/usr/bin/env bash
# A live try-out of #940: the apps behind the hostile proxy, in a browser you click through yourself.
#
#   ./demo.sh                     start what is not running, then open the browser (= start + browser)
#   ./demo.sh start [--mode real|stub] [--profile <name>]
#   ./demo.sh browser             a headed Chromium whose only route to the apps is the proxy
#   ./demo.sh profile <name>      switch the proxy: pass buffer-full buffer-16k gzip idle-30s deny-sse block-sse
#   ./demo.sh status | stop
#
# Apps (each started only if its directory is given and the port is free):
#   8231 React app, fixed (#942)          DEMO_REACT_DIR          (default: this checkout)
#   8234 classic chat 7.11, fixed (#941)  DEMO_CLASSIC_DIR        (a checkout of the 7.11 branch)
#   8235 React app, before                DEMO_REACT_BEFORE_DIR   (any checkout of master; the workaround script is removed by configuration)
#   8236 classic chat 7.11, before        DEMO_CLASSIC_BEFORE_DIR (a 7.11 checkout whose tl-ai-demo/src/main/webapp/script/chat-client.js is the original one)
# Mode "real" (default) answers with Anthropic: the key comes from tl_anthropic_api_key or the file
# DEMO_ANTHROPIC_KEY_FILE, and only the app processes see it. Mode "stub" answers with the scripted model.
set -euo pipefail

RIG="$(cd "$(dirname "$0")" && pwd)"
STATE="$RIG/target/demo"
PROXY_PORT=8232
STUB_PORT=8233
PROJECT="tlchat-sse-rig-$PROXY_PORT"
REAL_MODEL="${DEMO_REAL_MODEL:-claude-sonnet-4-6}"
mkdir -p "$STATE"

DEMO_REACT_DIR="${DEMO_REACT_DIR:-$(cd "$RIG/../.." && pwd)}"
declare -A DIR=([react]="$DEMO_REACT_DIR" [classic]="${DEMO_CLASSIC_DIR:-}"
  [react-before]="${DEMO_REACT_BEFORE_DIR:-}" [classic-before]="${DEMO_CLASSIC_BEFORE_DIR:-}")
declare -A PORT=([react]=8231 [classic]=8234 [react-before]=8235 [classic-before]=8236)
VARIANTS="react classic react-before classic-before"

compose() { docker compose -p "$PROJECT" -f "$RIG/docker-compose.yml" "$@"; }
listening_pid() { ss -ltnp 2>/dev/null | grep ":$1 " | grep -o 'pid=[0-9]*' | head -1 | cut -d= -f2; }
url() { if [[ $1 == classic* ]]; then echo "http://tlapp:${PORT[$1]}/tl-ai-demo/"; else echo "http://tlapp:${PORT[$1]}/tl-ai-demo/view/"; fi; }

# The rig configuration of an app: the classic line keeps its own (LLMService) in its checkout.
autoconf_of() {
  local variant=$1 source out="$STATE/$1/autoconf"
  if [[ $variant == classic* ]]; then source="${DIR[classic]:-${DIR[classic-before]}}/tl-chat/proxy-rig/autoconf"
  else source="$RIG/autoconf"; fi
  [ -d "$source" ] || source="$RIG/autoconf"
  rm -rf "$out"; mkdir -p "$out"; cp "$source"/*.xml "$out/"
  local model=scripted; [ "$MODE" = real ] && model="$REAL_MODEL"
  sed -i "s/default-model=\"scripted\"/default-model=\"$model\"/" "$out"/*.xml
  if [ "$variant" = react-before ]; then
    # Before #942: the page loads no window-stream workaround, as with the released tl-chat.
    cat > "$out/demo.before.config.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8" ?>
<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
	xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<services>
		<config service-class="com.top_logic.layout.react.resource.ClientResources">
			<instance class="com.top_logic.layout.react.resource.ClientResources">
				<resources>
					<script name="tl-window-stream" config:operation="remove"/>
				</resources>
			</instance>
		</config>
	</services>
</application>
EOF
  fi
  echo "$out"
}

start_app() {
  local variant=$1 dir=${DIR[$1]} port=${PORT[$1]}
  [ -n "$dir" ] || { echo "  $variant: no directory given, skipped"; return; }
  if [ -n "$(listening_pid "$port")" ]; then echo "  $variant: already running on $port"; return; fi
  local conf; conf=$(autoconf_of "$variant")
  local key="" mistral=dummy-local
  if [ "$MODE" = real ]; then
    key="${tl_anthropic_api_key:-}"
    [ -z "$key" ] && [ -n "${DEMO_ANTHROPIC_KEY_FILE:-}" ] && key="$(tr -d '\n\r ' < "$DEMO_ANTHROPIC_KEY_FILE")"
    [ -n "$key" ] || { echo "Mode real needs tl_anthropic_api_key or DEMO_ANTHROPIC_KEY_FILE." >&2; exit 1; }
  fi
  # The rig's autoconf names the variables (plain keys); the classic line reads them through
  # aliases into encrypted values, which take a plain key with the prefix "unencrypted:".
  if [[ $variant == classic* ]]; then
    [ -n "$key" ] && key="unencrypted:$key"
    mistral="unencrypted:$mistral"
  fi
  echo "  $variant: starting on $port (log $STATE/$variant/app.log) ..."
  (cd "$dir/tl-ai-demo" && exec env -u ANTHROPIC_API_KEY tl_anthropic_api_key="$key" tl_mistral_api_key="$mistral" \
    setsid mvn -o -q tl:resolve compile exec:java -Dtl.port="$port" -Dtl_autoconf_dir="$conf" \
    -Dtl_initial_password=root1234) > "$STATE/$variant/app.log" 2>&1 < /dev/null &
}

wait_app() {
  local variant=$1 port=${PORT[$1]}
  [ -n "${DIR[$1]}" ] || return 0
  for _ in $(seq 1 90); do
    if grep -q "Server started" "$STATE/$variant/app.log" 2>/dev/null || [ -s "$STATE/$variant/pid" ]; then break; fi
    sleep 4
  done
  local pid; pid=$(listening_pid "$port")
  if [ -n "$pid" ]; then echo "$pid" > "$STATE/$variant/pid"; else echo "  $variant did not start, see $STATE/$variant/app.log"; fi
}

set_profile() {
  PROFILE="$1" compose up -d --force-recreate >/dev/null 2>&1
  echo "$1" > "$STATE/profile"
  echo "Proxy profile: $1"
}

cmd_start() {
  MODE=real; local profile=buffer-full
  while [ $# -gt 0 ]; do
    case "$1" in --mode) MODE=$2; shift 2;; --profile) profile=$2; shift 2;; *) echo "unknown: $1"; exit 1;; esac
  done
  [ -d "$RIG/node_modules/playwright-core" ] || (cd "$RIG" && npm install --silent)
  if [ -z "$(listening_pid $STUB_PORT)" ]; then
    setsid python3 "$RIG/stub_llm.py" "$STUB_PORT" > "$STATE/stub.log" 2>&1 < /dev/null &
  fi
  for v in $VARIANTS; do mkdir -p "$STATE/$v"; start_app "$v"; done
  for v in $VARIANTS; do wait_app "$v"; done
  sleep 1; pid=$(listening_pid $STUB_PORT); [ -n "$pid" ] && echo "$pid" > "$STATE/stub.pid"
  echo "$MODE" > "$STATE/mode"
  set_profile "$profile"
  cmd_status
}

cmd_browser() {
  local profile_dir="$STATE/chromium"
  local urls=()
  for v in $VARIANTS; do [ -n "$(listening_pid "${PORT[$v]}")" ] && urls+=("$(url "$v")"); done
  setsid "${RIG_CHROMIUM:-/usr/bin/chromium}" --user-data-dir="$profile_dir" --no-first-run \
    --proxy-server="http://127.0.0.1:$PROXY_PORT" --proxy-bypass-list='<-loopback>' "${urls[@]}" \
    > "$STATE/chromium.log" 2>&1 < /dev/null &
  echo $! > "$STATE/chromium.pid"
  echo "Chromium started; its only route to the apps is the proxy on 127.0.0.1:$PROXY_PORT."
}

cmd_status() {
  echo
  echo "Mode: $(cat "$STATE/mode" 2>/dev/null || echo -)   proxy profile: $(cat "$STATE/profile" 2>/dev/null || echo -)"
  for v in $VARIANTS; do
    local state=stopped; [ -n "$(listening_pid "${PORT[$v]}")" ] && state=running
    printf '  %-15s %-8s %s\n' "$v" "$state" "$(url "$v")"
  done
  cat <<EOF
Login: root / root1234 (React: "Anmelden" top right; classic: the login form).
Switch the proxy:  $0 profile buffer-16k   (pass buffer-full buffer-16k gzip idle-30s deny-sse block-sse)
The fixed apps poll behind a hostile profile (console: "[TLReact] polling" / "[tl-chat] polling");
the "before" apps show the original behaviour (React: no login dialog; classic: no answer).
A tab that had to poll keeps polling: open a new window to see the stream again after "profile pass".
EOF
}

cmd_stop() {
  for f in "$STATE"/*/pid "$STATE"/stub.pid "$STATE"/chromium.pid; do
    [ -s "$f" ] || continue
    kill "$(cat "$f")" 2>/dev/null && echo "stopped $(cat "$f") ($f)"
    rm -f "$f"
  done
  compose down >/dev/null 2>&1 || true
}

case "${1:-}" in
  "") cmd_start; cmd_browser ;;
  start) shift; cmd_start "$@" ;;
  browser) cmd_browser ;;
  profile) set_profile "${2:?profile name}" ;;
  status) cmd_status ;;
  stop) cmd_stop ;;
  *) sed -n '2,20p' "$0"; exit 1 ;;
esac
