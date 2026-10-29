"""A forward HTTP proxy that behaves like a hostile corporate proxy.

Corporate proxies with content inspection (Zscaler, Netskope and others) break server-sent events
in different ways. Each profile models one of them. Select the profile with the environment
variable PROFILE:

  pass        Forwards every byte at once. The control profile.
  buffer-full Holds the whole response body until the upstream response ends. A stream that never
              ends never reaches the browser.
  buffer-16k  Forwards the body only in blocks of 16 KB (or at the end of the response).
  gzip        Compresses text responses without a flush (like an inspecting gateway that re-encodes
              content), so the browser gets bytes only when the compressor fills a block.
  idle-30s    Forwards at once, but closes a connection after 30 s without bytes from upstream.
  deny-sse    Refuses a response of type text/event-stream with 403 (after asking the server).
  block-sse   Refuses a request that asks for text/event-stream with 403, without forwarding it.

Only plain HTTP: the rig models what such a proxy does after it has broken up the TLS connection.
"""

import asyncio
import os
import sys
import zlib
from urllib.parse import urlsplit

PROFILE = os.environ.get("PROFILE", "pass")
LISTEN_HOST = os.environ.get("LISTEN_HOST", "0.0.0.0")
LISTEN_PORT = int(os.environ.get("LISTEN_PORT", "8080"))
BLOCK = 16 * 1024
IDLE_SECONDS = 30

HOP_HEADERS = {
    "connection", "keep-alive", "proxy-connection", "proxy-authorization", "te", "trailer",
    "transfer-encoding", "upgrade",
}


def log(*args):
    print(f"[{PROFILE}]", *args, file=sys.stderr, flush=True)


async def read_head(reader):
    """Reads a request or status line and its headers. Answers None at the end of the stream."""
    raw = await reader.readuntil(b"\r\n\r\n")
    lines = raw.decode("latin-1").split("\r\n")
    start = lines[0]
    headers = []
    for line in lines[1:]:
        if not line:
            continue
        name, _, value = line.partition(":")
        headers.append((name.strip(), value.strip()))
    return start, headers


def header(headers, name):
    name = name.lower()
    for key, value in headers:
        if key.lower() == name:
            return value
    return None


async def body_chunks(reader, headers, until_close):
    """Yields the decoded body of a message as it arrives."""
    encoding = (header(headers, "transfer-encoding") or "").lower()
    length = header(headers, "content-length")
    if "chunked" in encoding:
        while True:
            size_line = await reader.readuntil(b"\r\n")
            size = int(size_line.split(b";")[0].strip(), 16)
            if size == 0:
                # Trailers, up to the empty line.
                while (await reader.readuntil(b"\r\n")) != b"\r\n":
                    pass
                return
            data = await reader.readexactly(size)
            await reader.readexactly(2)
            yield data
    elif length is not None:
        remaining = int(length)
        while remaining > 0:
            data = await reader.read(min(remaining, 65536))
            if not data:
                return
            remaining -= len(data)
            yield data
    elif until_close:
        while True:
            data = await reader.read(65536)
            if not data:
                return
            yield data


async def read_body(reader, headers):
    parts = [chunk async for chunk in body_chunks(reader, headers, until_close=False)]
    return b"".join(parts)


class ClientGone(Exception):
    pass


async def send(writer, data):
    if not data:
        return
    writer.write(data)
    try:
        await writer.drain()
    except (ConnectionError, OSError) as ex:
        raise ClientGone() from ex


def chunk(data):
    return b"%x\r\n" % len(data) + data + b"\r\n"


async def relay(client_reader, client_writer):
    peer = client_writer.get_extra_info("peername")
    try:
        while True:
            try:
                start, headers = await read_head(client_reader)
            except (asyncio.IncompleteReadError, ConnectionError):
                return
            method, target, _version = start.split(" ", 2)
            if method == "CONNECT":
                await send(client_writer, b"HTTP/1.1 405 Method Not Allowed\r\nContent-Length: 0\r\n\r\n")
                continue
            body = await read_body(client_reader, headers)
            if PROFILE == "block-sse" and "text/event-stream" in (header(headers, "accept") or ""):
                log(method, target, "event stream blocked")
                text = b"Blocked by policy: event streams are not allowed.\n"
                await send(client_writer, b"HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n"
                           + b"Content-Length: %d\r\n\r\n" % len(text) + text)
                continue
            keep = await forward(method, target, headers, body, client_writer)
            if not keep:
                return
    except ClientGone:
        pass
    except Exception as ex:  # noqa: BLE001 - a proxy must survive any single exchange
        log("exchange failed", peer, repr(ex))
    finally:
        client_writer.close()


async def forward(method, target, headers, body, client_writer):
    """Forwards one request and its answer. Answers whether the client connection stays open."""
    url = urlsplit(target)
    host = url.hostname
    port = url.port or 80
    path = url.path or "/"
    if url.query:
        path += "?" + url.query
    upstream_reader, upstream_writer = await asyncio.open_connection(host, port)
    try:
        out = [f"{method} {path} HTTP/1.1"]
        for name, value in headers:
            if name.lower() in HOP_HEADERS or name.lower() == "content-length":
                continue
            out.append(f"{name}: {value}")
        out.append("Connection: close")
        if body or method in ("POST", "PUT", "PATCH", "DELETE"):
            out.append(f"Content-Length: {len(body)}")
        upstream_writer.write(("\r\n".join(out) + "\r\n\r\n").encode("latin-1") + body)
        await upstream_writer.drain()

        status_line, response_headers = await read_head(upstream_reader)
        status = int(status_line.split(" ")[1])
        content_type = (header(response_headers, "content-type") or "").lower()
        has_body = method != "HEAD" and status not in (204, 304) and not 100 <= status < 200
        streaming = "text/event-stream" in content_type

        if streaming:
            log(method, path, status, "event stream")

        if PROFILE == "deny-sse" and streaming:
            text = b"Blocked by policy: content type text/event-stream is not allowed.\n"
            await send(client_writer, b"HTTP/1.1 403 Forbidden\r\nContent-Type: text/plain\r\n"
                       + b"Content-Length: %d\r\n\r\n" % len(text) + text)
            return True

        kept = [(n, v) for n, v in response_headers
                if n.lower() not in HOP_HEADERS and n.lower() != "content-length"]

        def head(extra):
            lines = [status_line] + [f"{n}: {v}" for n, v in kept + extra]
            return ("\r\n".join(lines) + "\r\n\r\n").encode("latin-1")

        chunks = body_chunks(upstream_reader, response_headers, until_close=True)

        if not has_body:
            await send(client_writer, head([("Content-Length", "0")]))
            return True

        if PROFILE == "buffer-full":
            data = b"".join([c async for c in chunks])
            await send(client_writer, head([("Content-Length", str(len(data)))]) + data)
            return True

        encoded = (header(response_headers, "content-encoding") or "identity").lower() != "identity"
        if PROFILE == "gzip" and not encoded and (content_type.startswith("text/") or "json" in content_type
                                                  or "javascript" in content_type):
            await send(client_writer, head([("Content-Encoding", "gzip"), ("Transfer-Encoding", "chunked")]))
            compressor = zlib.compressobj(6, zlib.DEFLATED, 31)
            async for data in chunks:
                packed = compressor.compress(data)
                if packed:
                    await send(client_writer, chunk(packed))
            rest = compressor.flush()
            await send(client_writer, (chunk(rest) if rest else b"") + b"0\r\n\r\n")
            return True

        await send(client_writer, head([("Transfer-Encoding", "chunked")]))
        if PROFILE == "buffer-16k":
            pending = b""
            async for data in chunks:
                pending += data
                while len(pending) >= BLOCK:
                    await send(client_writer, chunk(pending[:BLOCK]))
                    pending = pending[BLOCK:]
            if pending:
                await send(client_writer, chunk(pending))
        elif PROFILE == "idle-30s":
            iterator = chunks.__aiter__()
            while True:
                try:
                    data = await asyncio.wait_for(iterator.__anext__(), IDLE_SECONDS)
                except StopAsyncIteration:
                    break
                except asyncio.TimeoutError:
                    log(method, path, f"idle for {IDLE_SECONDS} s, closing")
                    return False
                await send(client_writer, chunk(data))
        else:
            async for data in chunks:
                await send(client_writer, chunk(data))
        await send(client_writer, b"0\r\n\r\n")
        return True
    finally:
        upstream_writer.close()


async def main():
    server = await asyncio.start_server(relay, LISTEN_HOST, LISTEN_PORT)
    log(f"listening on {LISTEN_PORT}")
    async with server:
        await server.serve_forever()


if __name__ == "__main__":
    asyncio.run(main())
