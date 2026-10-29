# Proxy rig: the chat behind a hostile corporate proxy (#940)

`./run-matrix.sh [out-dir]` starts what is missing (scripted model on 8233, tl-ai-demo on 8231 with
`autoconf/`), then runs the app once per proxy profile and prints PASS or FAIL per profile.
Needs docker, node and `/usr/bin/chromium` (`RIG_CHROMIUM` for another path).

The proxy (`proxy.py`, docker, 127.0.0.1:8232) is the only route: the browser opens `http://tlapp:8231`,
and `tlapp` resolves only inside the proxy container. Profiles: `pass`, `buffer-full` (holds the
response until it ends), `buffer-16k`, `gzip` (compresses without flush), `idle-30s`, `deny-sse`
(403 for a `text/event-stream` response), `block-sse` (403 for a request that asks for one); `direct`
is the control run without proxy. A `classic-` profile runs the classic UI's Albert tab: it signs in
through `pass` (engine 8 has only the React login), then turns the proxy hostile. `RIG_PROFILES` selects.
`chat-check.mjs` signs in, sends "ping <token>", waits for "pong <token>" (the stub's answer), sends a
second message and checks a navigation. Screenshots and logs go to the out-dir. The rig runs over
plain HTTP, which is no secure context: the chat has to work there too.

`./demo.sh` is the live try-out: it starts the apps named by `DEMO_*_DIR` (fixed and before, React
and classic 7.11) behind the proxy (`buffer-full` by default, `./demo.sh profile <name>` to switch),
answers with Anthropic (`--mode real`, key from `tl_anthropic_api_key` or `DEMO_ANTHROPIC_KEY_FILE`)
or the stub (`--mode stub`), and opens a Chromium whose only route is the proxy. See its header.
