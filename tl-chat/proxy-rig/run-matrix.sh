#!/usr/bin/env bash
# Runs the chat through every proxy profile and prints pass/fail per profile. See README.md.
set -euo pipefail

RIG="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$RIG/../.." && pwd)"
OUT="${1:-$RIG/target/matrix-$(date +%Y%m%d-%H%M%S)}"
export RIG_APP_PORT="${RIG_APP_PORT:-8231}" RIG_PROXY_PORT="${RIG_PROXY_PORT:-8232}"
STUB_PORT="${RIG_STUB_PORT:-8233}"
# A profile name with the prefix "classic-" runs the classic UI with its embedded chat tab.
PROFILES="${RIG_PROFILES:-direct pass buffer-full buffer-16k gzip idle-30s deny-sse block-sse classic-pass classic-buffer-full classic-buffer-16k classic-gzip classic-deny-sse classic-block-sse}"
PROJECT="tlchat-sse-rig-$RIG_PROXY_PORT"
mkdir -p "$OUT"

[ -d "$RIG/node_modules/playwright-core" ] || (cd "$RIG" && npm install --silent)

app_up() { curl -s -o /dev/null -m 5 "http://127.0.0.1:$RIG_APP_PORT/tl-ai-demo/"; }

STARTED=()
cleanup() {
  docker compose -p "$PROJECT" -f "$RIG/docker-compose.yml" down >/dev/null 2>&1 || true
  for pid in "${STARTED[@]}"; do kill "$pid" 2>/dev/null || true; done
}
trap cleanup EXIT

if ! curl -s -o /dev/null -m 2 "http://127.0.0.1:$STUB_PORT/v1/models"; then
  python3 "$RIG/stub_llm.py" "$STUB_PORT" > "$OUT/stub-llm.log" 2>&1 &
  STARTED+=($!)
fi

if ! app_up; then
  echo "Starting tl-ai-demo on port $RIG_APP_PORT (log: $OUT/app.log) ..."
  (cd "$REPO/tl-ai-demo" && env -u tl_anthropic_api_key tl_mistral_api_key=dummy-local \
    mvn -o -q tl:resolve compile exec:java -Dtl.port="$RIG_APP_PORT" \
    -Dtl_autoconf_dir="$RIG/autoconf" -Dtl_initial_password=root1234 > "$OUT/app.log" 2>&1) &
  STARTED+=($!)
  for _ in $(seq 1 120); do grep -q "Server started" "$OUT/app.log" 2>/dev/null && break; sleep 5; done
  app_up || { echo "The application did not start, see $OUT/app.log"; exit 1; }
fi

compose() { docker compose -p "$PROJECT" -f "$RIG/docker-compose.yml" "$@"; }
proxy_ready() {
  for _ in $(seq 1 20); do curl -s -o /dev/null -m 2 -x "http://127.0.0.1:$RIG_PROXY_PORT" \
    "http://tlapp:$RIG_APP_PORT/tl-ai-demo/" && return; sleep 0.5; done
}

printf '%-20s %-6s %-7s %-5s %-8s %-8s %-9s %-10s %s\n' PROFILE LOGIN ANSWER NAV CLASSIC FIRST_S SECOND_S TRANSPORT RESULT
FAILED=0
for profile in $PROFILES; do
  wait_before=3
  [ "$profile" = idle-30s ] && wait_before=45
  if [ "$profile" = direct ]; then
    line=$(cd "$RIG" && RIG_WAIT_BEFORE_SEND=$wait_before node chat-check.mjs "$profile" "$OUT" --direct)
  elif [ "${profile#classic-}" != "$profile" ]; then
    # Signs in through a friendly proxy (engine 8 has only the React login), then turns it hostile.
    PROFILE=pass compose up -d --force-recreate >/dev/null 2>&1; proxy_ready
    hostile="PROFILE=${profile#classic-} docker compose -p $PROJECT -f $RIG/docker-compose.yml up -d --force-recreate"
    hostile="$hostile && sleep 2"
    line=$(cd "$RIG" && RIG_HOSTILE_CMD="$hostile" RIG_WAIT_BEFORE_SEND=$wait_before \
      node chat-check.mjs "$profile" "$OUT" --classic)
    compose logs --no-color > "$OUT/$profile.proxy.log" 2>&1
  else
    PROFILE="$profile" compose up -d --force-recreate >/dev/null 2>&1; proxy_ready
    line=$(cd "$RIG" && RIG_WAIT_BEFORE_SEND=$wait_before node chat-check.mjs "$profile" "$OUT")
    compose logs --no-color > "$OUT/$profile.proxy.log" 2>&1
  fi
  echo "$line" >> "$OUT/results.jsonl"
  python3 - "$line" <<'PY'
import json, sys
r = json.loads(sys.argv[1])
ok = r["login"] and r["answer"] and r.get("classic", True) and r.get("nav", True)
classic = str(r["classic"]).lower() if "classic" in r else "-"
nav = str(r["nav"]).lower() if "nav" in r else "-"
print(f'{r["label"]:<20} {str(r["login"]).lower():<6} {str(r["answer"]).lower():<7} {nav:<5} {classic:<8} '
      f'{r["seconds"] if r["seconds"] is not None else "-":<8} {r["seconds2"] if r["seconds2"] is not None else "-":<9} {r["transport"]:<10} {"PASS" if ok else "FAIL"}')
PY
  echo "$line" | grep -q '"answer":true' || FAILED=1
done
echo "Evidence (screenshots, logs): $OUT"
exit $FAILED
