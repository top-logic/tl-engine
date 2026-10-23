"""A scripted OpenAI-compatible model for the proxy rig: deterministic, free, no network.

Answers "/v1/models" with the one model "scripted" and streams every chat completion as
"pong <last word of the user's message>", in small pieces over about two seconds, so that the
answer is a real stream and the rig can tell one answer from another.

Usage: python3 stub_llm.py PORT
"""

import json
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


def user_text(request):
    for message in reversed(request.get("messages", [])):
        if message.get("role") != "user":
            continue
        content = message.get("content")
        if isinstance(content, list):
            content = " ".join(part.get("text", "") for part in content if isinstance(part, dict))
        return content or ""
    return ""


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):
        sys.stderr.write("stub-llm: " + fmt % args + "\n")

    def do_GET(self):
        if self.path.rstrip("/").endswith("/models"):
            self.reply(json.dumps({"object": "list", "data": [{"id": "scripted", "object": "model"}]}))
        else:
            self.send_error(404)

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0"))
        request = json.loads(self.rfile.read(length) or b"{}")
        if not self.path.rstrip("/").endswith("/chat/completions"):
            self.send_error(404)
            return
        words = user_text(request).split()
        # The user's text ends the turn context block; its last word is the token the rig sent.
        token = words[-1] if words else "nothing"
        pieces = ["pong", " ", token, ".", " Diese", " Antwort", " kommt", " vom", " Stub-Modell."]
        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream")
        self.send_header("Cache-Control", "no-cache")
        self.send_header("Transfer-Encoding", "chunked")
        self.end_headers()
        base = {"id": "stub", "object": "chat.completion.chunk", "created": int(time.time()), "model": "scripted"}
        self.event({**base, "choices": [{"index": 0, "delta": {"role": "assistant", "content": ""}}]})
        for piece in pieces:
            time.sleep(0.2)
            self.event({**base, "choices": [{"index": 0, "delta": {"content": piece}}]})
        self.event({**base, "choices": [{"index": 0, "delta": {}, "finish_reason": "stop"}],
                    "usage": {"prompt_tokens": 100, "completion_tokens": len(pieces), "total_tokens": 100 + len(pieces)}})
        self.write_chunk(b"data: [DONE]\n\n")
        self.write_chunk(b"")

    def event(self, payload):
        self.write_chunk(("data: " + json.dumps(payload) + "\n\n").encode("utf-8"))

    def write_chunk(self, data):
        self.wfile.write(b"%x\r\n" % len(data) + data + b"\r\n")
        self.wfile.flush()

    def reply(self, text):
        data = text.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)


if __name__ == "__main__":
    ThreadingHTTPServer(("127.0.0.1", int(sys.argv[1])), Handler).serve_forever()
